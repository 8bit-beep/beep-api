package com.b.beep.domain.user.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import java.time.DayOfWeek

data class AddStudentScheduleRequest(
    val dayOfWeek: DayOfWeek,
    val period: Int,
    val type: AttendanceType,
    val room: Room
)
