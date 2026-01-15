package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType

data class StatusResponse(
    val period: Int,
    val status: AttendanceType
)