package com.b.beep.domain.student.repository

import com.b.beep.domain.user.entity.QStudentInfoEntity
import com.b.beep.domain.user.entity.QUserEntity
import com.b.beep.domain.user.entity.QStudentScheduleEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.user.domain.UserRole
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class StudentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun findAllByStatusAndGradeAndCls(
        grade: Int,
        cls: Int,
        status: AttendanceType? = null,
    ): List<UserEntity> {
        val userEntity = QUserEntity.userEntity
        val studentInfoEntity = QStudentInfoEntity.studentInfoEntity

        return queryFactory
            .selectFrom(userEntity)
            .join(studentInfoEntity)
            .on(studentInfoEntity.user.id.eq(userEntity.id))
            .where(
                studentInfoEntity.grade.eq(grade),
                studentInfoEntity.cls.eq(cls),
                status?.let { userEntity.currentStatus.eq(it) },
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
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val period = PeriodResolver.getCurrentPeriod()

        return queryFactory
            .selectFrom(userEntity)
            .join(scheduleEntity)
            .on(scheduleEntity.user.id.eq(userEntity.id))
            .where(
                scheduleEntity.room.eq(room),
                scheduleEntity.type.eq(type),
                scheduleEntity.dayOfWeek.eq(dayOfWeek),
                scheduleEntity.period.eq(period),
                status?.let { userEntity.currentStatus.eq(it) },
                userEntity.role.eq(UserRole.STUDENT)
            )
            .fetch()
    }
}
