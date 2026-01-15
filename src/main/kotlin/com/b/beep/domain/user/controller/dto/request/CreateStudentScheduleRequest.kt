package com.b.beep.domain.user.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import java.time.DayOfWeek

data class CreateStudentScheduleRequest(
    val dayOfWeek: DayOfWeek,
    val period: Int,
    val type: AttendanceType,
    val roomId: Long
)
