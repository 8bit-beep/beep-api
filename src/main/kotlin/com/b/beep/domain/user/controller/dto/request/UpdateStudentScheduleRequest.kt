package com.b.beep.domain.user.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import java.time.DayOfWeek

data class UpdateStudentScheduleRequest(
    val dayOfWeek: DayOfWeek? = null,
    val period: Int? = null,
    val type: AttendanceType? = null,
    val room: Room? = null
)
