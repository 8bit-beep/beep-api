package com.b.beep.domain.attendance.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType

data class CreateAttendanceRequest(
    val roomId: Long,
    val attendanceType: AttendanceType
)
