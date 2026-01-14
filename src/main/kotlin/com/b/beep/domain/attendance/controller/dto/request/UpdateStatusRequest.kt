package com.b.beep.domain.attendance.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType

data class UpdateStatusRequest(
    val grade: Int,
    val cls: Int,
    val num: Int,
    val status: AttendanceType
)
