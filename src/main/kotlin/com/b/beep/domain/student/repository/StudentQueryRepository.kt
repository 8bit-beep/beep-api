package com.b.beep.domain.student.repository

import com.b.beep.domain.user.entity.QStudentInfoEntity
import com.b.beep.domain.user.entity.QUserEntity
import com.b.beep.domain.user.entity.QStudentScheduleEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.entity.QAttendanceEntity
import com.b.beep.domain.user.domain.UserRole
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class StudentQueryRepository(
    private val queryFactory: JPAQueryFactory,
    private val periodResolver: PeriodResolver
) {
    fun findAllByStatusAndGradeAndCls(
        grade: Int,
        cls: Int,
        status: AttendanceType? = null,
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity
        val today = LocalDate.now()
        val period = periodResolver.getCurrentPeriod()

        val query = queryFactory
            .selectFrom(userEntity)
            .join(studentInfoEntity)
            .on(studentInfoEntity.user.id.eq(userEntity.id))

        if (status != null && period > 0) {
            query.leftJoin(attendanceEntity)
                .on(
                    attendanceEntity.user.id.eq(userEntity.id),
                    attendanceEntity.date.eq(today),
                    attendanceEntity.period.eq(period)
                )
        }

        return query
            .where(
                studentInfoEntity.grade.eq(grade),
                studentInfoEntity.cls.eq(cls),
                status?.let { if (period > 0) attendanceEntity.type.eq(it) else null },
                userEntity.role.eq(UserRole.STUDENT)
            )
            .fetch()
    }

    fun findAllByStatusAndRoomAndType(
        room: Room,
        type: AttendanceType,
        status: AttendanceType? = null,
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val scheduleEntity = QStudentScheduleEntity.studentScheduleEntity
        val attendanceEntity = QAttendanceEntity.attendanceEntity
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val period = periodResolver.getCurrentPeriod()

        val query = queryFactory
            .selectFrom(userEntity)
            .join(scheduleEntity)
            .on(scheduleEntity.user.id.eq(userEntity.id))

        if (status != null && period > 0) {
            query.leftJoin(attendanceEntity)
                .on(
                    attendanceEntity.user.id.eq(userEntity.id),
                    attendanceEntity.date.eq(today),
                    attendanceEntity.period.eq(period)
                )
        }

        return query
            .where(
                scheduleEntity.room.eq(room),
                scheduleEntity.type.eq(type),
                scheduleEntity.dayOfWeek.eq(dayOfWeek),
                scheduleEntity.period.eq(period),
                status?.let { if (period > 0) attendanceEntity.type.eq(it) else null },
                userEntity.role.eq(UserRole.STUDENT)
            )
            .fetch()
    }
}
