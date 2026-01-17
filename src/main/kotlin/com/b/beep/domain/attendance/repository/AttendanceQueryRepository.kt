package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.QAttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.QStudentInfoEntity
import com.b.beep.domain.user.domain.entity.QStudentScheduleEntity
import com.b.beep.domain.user.domain.entity.QUserEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AttendanceQueryRepository(
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val queryFactory: JPAQueryFactory,
) {
    fun findCurrentStatus(user: UserEntity): AttendanceType {
        val today = LocalDate.now()
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNull() ?: return AttendanceType.NOT_ATTEND

        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, today)
            ?: return AttendanceType.NOT_ATTEND

        return attendance.type
    }

    fun findAllByFilters(
        room: RoomEntity? = null,
        status: AttendanceType? = null,
        grade: Int? = null,
        classNumber: Int? = null,
        scheduleOnly: Boolean? = null
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNull()

        val query = queryFactory
            .selectFrom(userEntity)
            .distinct()
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(userEntity.id))

        if (room != null && scheduleOnly == true && checkpoint != null) {
            query.join(scheduleEntity).on(scheduleEntity.user.id.eq(userEntity.id))
        }

        if (status != null && checkpoint != null) {
            query.leftJoin(attendanceEntity).on(
                attendanceEntity.user.id.eq(userEntity.id),
                attendanceEntity.date.eq(today),
                attendanceEntity.checkpoint.id.eq(checkpoint.id)
            )
        }

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(userEntity.role.eq(UserRole.STUDENT))

        grade?.let { whereBuilder.and(studentInfoEntity.grade.eq(it)) }
        classNumber?.let { whereBuilder.and(studentInfoEntity.classNumber.eq(it)) }

        if (room != null && scheduleOnly == true && checkpoint != null) {
            whereBuilder.and(scheduleEntity.room.id.eq(room.id))
            whereBuilder.and(scheduleEntity.dayOfWeek.eq(dayOfWeek))
            whereBuilder.and(scheduleEntity.checkpoint.id.eq(checkpoint.id))
        }

        if (status != null && checkpoint != null) {
            when (status) {
                AttendanceType.NOT_ATTEND -> {
                    whereBuilder.and(attendanceEntity.id.isNull)
                }

                AttendanceType.OUTGOING -> {
                    whereBuilder.and(
                        attendanceEntity.type.eq(AttendanceType.OUTGOING)
                            .or(attendanceEntity.absence.isNotNull)
                    )
                }

                else -> {
                    whereBuilder.and(attendanceEntity.type.eq(status))
                }
            }
        }

        return query.where(whereBuilder).fetch()
    }
}
