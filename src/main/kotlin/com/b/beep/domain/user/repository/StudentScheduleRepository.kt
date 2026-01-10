package com.b.beep.domain.user.repository

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import java.time.DayOfWeek
import com.b.beep.domain.user.entity.StudentScheduleEntity
import com.b.beep.domain.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentScheduleRepository : JpaRepository<StudentScheduleEntity, Long> {
    fun findAllByUser(user: UserEntity): List<StudentScheduleEntity>
    fun findByUserAndDayOfWeekAndPeriod(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        period: Int
    ): StudentScheduleEntity?
    fun findByUserAndDayOfWeekAndPeriodAndType(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        period: Int,
        type: AttendanceType
    ): StudentScheduleEntity?
    fun existsByUserAndDayOfWeekAndPeriod(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        period: Int
    ): Boolean
}
