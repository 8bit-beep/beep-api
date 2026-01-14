package com.b.beep.domain.attendance.repository

import com.b.beep.domain.user.entity.QStudentInfoEntity
import com.b.beep.domain.user.entity.QUserEntity
import com.b.beep.domain.user.entity.QStudentScheduleEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.entity.QAttendanceEntity
import com.b.beep.domain.user.domain.UserRole
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AttendanceStudentQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val periodResolver: PeriodResolver
) {
    fun findAllByFilters(
        room: Room? = null,
        type: AttendanceType? = null,
        status: AttendanceType? = null,
        grade: Int? = null,
        cls: Int? = null
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity

        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val period = periodResolver.getCurrentPeriod()

        val query = queryFactory
            .selectFrom(userEntity)
            .distinct()
            .join(studentInfoEntity).on(studentInfoEntity.user.id.eq(userEntity.id))

        if (room != null && type != null) {
            query.join(scheduleEntity).on(scheduleEntity.user.id.eq(userEntity.id))
        }

        if (status != null && period > 0) {
            query.leftJoin(attendanceEntity).on(
                attendanceEntity.user.id.eq(userEntity.id),
                attendanceEntity.date.eq(today),
                attendanceEntity.period.eq(period)
            )
        }

        val whereBuilder = BooleanBuilder()
        whereBuilder.and(userEntity.role.eq(UserRole.STUDENT))

        grade?.let { whereBuilder.and(studentInfoEntity.grade.eq(it)) }
        cls?.let { whereBuilder.and(studentInfoEntity.cls.eq(it)) }

        if (room != null && type != null) {
            whereBuilder.and(scheduleEntity.room.eq(room))
            whereBuilder.and(scheduleEntity.type.eq(type))
            whereBuilder.and(scheduleEntity.dayOfWeek.eq(dayOfWeek))
            whereBuilder.and(scheduleEntity.period.eq(period))
        }

        if (status != null && period > 0) {
            whereBuilder.and(attendanceEntity.type.eq(status))
        }

        return query.where(whereBuilder).fetch()
    }
}
