package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QStudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.QStudentScheduleEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Repository
class AttendanceQueryRepository(
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val queryFactory: JPAQueryFactory,
    private val checkpointRepository: AttendanceCheckpointRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun findCurrentStatus(user: UserEntity, grade: Int): AttendanceTypeEntity? {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val checkpoint = try {
            checkpointResolver.getCurrentAttendableCheckpointOrNull(grade)
                ?: checkpointResolver.getCurrentCheckpointOrNearest(grade, today.dayOfWeek)
        } catch (e: Exception) {
            return null
        }

        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, today)
            ?: return null

        return attendance.type
    }

    fun findAllByFilters(
        date: LocalDate?,
        checkpoint: AttendanceCheckpointEntity? = null,
        room: RoomEntity? = null,
        status: AttendanceTypeEntity? = null,
        grade: Int? = null,
        classNumber: Int? = null,
        isCurrentCheckpoint: Boolean = true
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity

        val targetDate = date ?: LocalDate.now(ZoneId.of("Asia/Seoul"))
        val dayOfWeek = targetDate.dayOfWeek
        val targetCheckpoint = checkpoint ?: if (isCurrentCheckpoint) {
            checkpointResolver.getCurrentCheckpointOrNearest()
        } else { null }

        log.info("[findAllByFilters] date=$targetDate, room=${room?.id}, checkpoint=${targetCheckpoint?.id}, dayOfWeek=$dayOfWeek")

        val query = queryFactory
            .selectFrom(userEntity)
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(userEntity.id))

        if (status != null && targetCheckpoint != null) {
            query.leftJoin(attendanceEntity).on(
                attendanceEntity.user.id.eq(userEntity.id),
                attendanceEntity.date.eq(targetDate),
                attendanceEntity.checkpoint.id.eq(targetCheckpoint.id)
            )
        }

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(userEntity.role.eq(UserRole.STUDENT))
        whereBuilder.and(userEntity.isDeleted.eq(false))

        grade?.let { whereBuilder.and(studentInfoEntity.grade.eq(it)) }
        classNumber?.let { whereBuilder.and(studentInfoEntity.classNumber.eq(it)) }

        if (room != null) {
            val overlappingIds = if (targetCheckpoint != null) {
                checkpointRepository.findAllByIsDeletedFalse()
                    .filter { it.startAt < targetCheckpoint.endAt && targetCheckpoint.startAt < it.endAt }
                    .mapNotNull { it.id }
            } else emptyList()

            val subQuery = JPAExpressions
                .selectOne()
                .from(scheduleEntity)
                .where(
                    scheduleEntity.user.id.eq(userEntity.id),
                    scheduleEntity.room.id.eq(room.id),
                    scheduleEntity.dayOfWeek.eq(dayOfWeek),
                    if (targetCheckpoint != null && overlappingIds.isNotEmpty())
                        scheduleEntity.checkpoint.id.`in`(overlappingIds)
                    else null
                )
            whereBuilder.and(subQuery.exists())
        }

        if (status != null && targetCheckpoint != null) {
            if (status.name == AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME) {
                whereBuilder.and(attendanceEntity.id.isNull)
            } else if (status.name == "OUTGOING") {
                whereBuilder.and(
                    attendanceEntity.type.id.eq(status.id)
                        .or(attendanceEntity.absence.isNotNull)
                )
            } else {
                whereBuilder.and(attendanceEntity.type.id.eq(status.id))
            }
        }

        return query
            .where(whereBuilder)
            .orderBy(
                studentInfoEntity.grade.asc(),
                studentInfoEntity.classNumber.asc(),
                studentInfoEntity.num.asc()
            )
            .fetch()
    }

    fun findScheduledRoomIdsByGradeAndCheckpoint(
        grade: Int,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity
    ): Set<Long> {
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity

        return queryFactory
            .select(scheduleEntity.room.id)
            .from(scheduleEntity)
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(scheduleEntity.user.id))
            .where(
                scheduleEntity.user.isDeleted.eq(false),
                scheduleEntity.dayOfWeek.eq(dayOfWeek),
                scheduleEntity.checkpoint.id.eq(checkpoint.id),
                studentInfoEntity.grade.eq(grade)
            )
            .distinct()
            .fetch()
            .filterNotNull()
            .toSet()
    }

    fun findClubMajorityRoomIdsByDayCheckpoint(
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity,
        clubType: AttendanceTypeEntity
    ): Set<Long> {
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val roomIdPath = scheduleEntity.room.id
        val totalCountExpression = roomIdPath.count()
        val clubCountExpression = CaseBuilder()
            .`when`(scheduleEntity.type.id.eq(clubType.id)).then(1L).otherwise(0L)
            .sum()

        return queryFactory
            .select(roomIdPath, totalCountExpression, clubCountExpression)
            .from(scheduleEntity)
            .where(
                scheduleEntity.user.isDeleted.eq(false),
                scheduleEntity.dayOfWeek.eq(dayOfWeek),
                scheduleEntity.checkpoint.id.eq(checkpoint.id)
            )
            .groupBy(roomIdPath)
            .fetch()
            .mapNotNull { tuple ->
                val roomId = tuple.get(roomIdPath) ?: return@mapNotNull null
                val totalCount = tuple.get(totalCountExpression) ?: 0L
                val clubCount = tuple.get(clubCountExpression) ?: 0L
                if (clubCount * 2 > totalCount) roomId else null
            }
            .toSet()
    }

    fun findActivityRoomIdsByGradeDayOrCommonAndType(
        grade: Int,
        dayOfWeek: DayOfWeek,
        type: AttendanceTypeEntity
    ): Set<Long> {
        val activityRoomEntity = QStudentActivityRoomEntity.studentActivityRoomEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity

        return queryFactory
            .select(activityRoomEntity.room.id)
            .from(activityRoomEntity)
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(activityRoomEntity.user.id))
            .where(
                activityRoomEntity.user.isDeleted.eq(false),
                activityRoomEntity.dayOfWeek.eq(dayOfWeek)
                    .or(activityRoomEntity.dayOfWeek.isNull),
                activityRoomEntity.type.id.eq(type.id),
                studentInfoEntity.grade.eq(grade)
            )
            .distinct()
            .fetch()
            .filterNotNull()
            .toSet()
    }
}
