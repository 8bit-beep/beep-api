package com.b.beep.domain.user.repository

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek

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
