package com.b.beep.domain.attendance.repository

import com.b.beep.domain.absence.domain.entity.QAbsenceEntity
import com.b.beep.domain.absence.repository.AbsenceRepository
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
    private val absenceRepository: AbsenceRepository,
    private val queryFactory: JPAQueryFactory,
) {
    fun findCurrentStatus(user: UserEntity): AttendanceType {
        val today = LocalDate.now()
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNull() ?: return AttendanceType.NOT_ATTEND

        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, today)
        if (attendance != null) return attendance.type

        val isAbsent = absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            user.id!!, today, today
        )
        return if (isAbsent) AttendanceType.OUTGOING else AttendanceType.NOT_ATTEND
    }

    fun findAllByFilters(
        room: RoomEntity? = null,
        type: AttendanceType? = null,
        status: AttendanceType? = null,
        grade: Int? = null,
        classNumber: Int? = null
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity
        val absenceEntity = QAbsenceEntity.absenceEntity

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNull()

        val query = queryFactory
            .selectFrom(userEntity)
            .distinct()
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(userEntity.id))

        if (room != null && type != null && checkpoint != null) {
            query.join(scheduleEntity).on(scheduleEntity.user.id.eq(userEntity.id))
        }

        if (status != null && checkpoint != null) {
            query.leftJoin(attendanceEntity).on(
                attendanceEntity.user.id.eq(userEntity.id),
                attendanceEntity.date.eq(today),
                attendanceEntity.checkpoint.id.eq(checkpoint.id)
            )
            query.leftJoin(absenceEntity).on(
                absenceEntity.user.id.eq(userEntity.id),
                absenceEntity.startDate.loe(today),
                absenceEntity.endDate.goe(today)
            )
        }

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(userEntity.role.eq(UserRole.STUDENT))

        grade?.let { whereBuilder.and(studentInfoEntity.grade.eq(it)) }
        classNumber?.let { whereBuilder.and(studentInfoEntity.classNumber.eq(it)) }

        if (room != null && type != null && checkpoint != null) {
            whereBuilder.and(scheduleEntity.room.id.eq(room.id))
            whereBuilder.and(scheduleEntity.type.eq(type))
            whereBuilder.and(scheduleEntity.dayOfWeek.eq(dayOfWeek))
            whereBuilder.and(scheduleEntity.checkpoint.id.eq(checkpoint.id))
        }

        if (status != null && checkpoint != null) {
            when (status) {
                AttendanceType.NOT_ATTEND -> {
                    whereBuilder.and(attendanceEntity.id.isNull)
                    whereBuilder.and(absenceEntity.id.isNull)
                }

                AttendanceType.OUTGOING -> {
                    whereBuilder.and(
                        attendanceEntity.type.eq(AttendanceType.OUTGOING)
                            .or(absenceEntity.id.isNotNull)
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
