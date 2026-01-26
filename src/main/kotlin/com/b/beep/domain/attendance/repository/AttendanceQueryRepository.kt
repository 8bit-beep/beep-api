package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QStudentScheduleEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZoneId

@Repository
class AttendanceQueryRepository(
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val queryFactory: JPAQueryFactory,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun findCurrentStatus(user: UserEntity): AttendanceTypeEntity? {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNearest()

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
            .distinct()
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(userEntity.id))

        if (room != null) {
            query.join(scheduleEntity).on(scheduleEntity.user.id.eq(userEntity.id))
        }

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
            whereBuilder.and(scheduleEntity.room.id.eq(room.id))
            whereBuilder.and(scheduleEntity.dayOfWeek.eq(dayOfWeek))
            if (targetCheckpoint != null) {
                whereBuilder.and(scheduleEntity.checkpoint.id.eq(targetCheckpoint.id))
            }
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
            .fetch()
    }
}
