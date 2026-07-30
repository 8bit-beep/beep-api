package com.b.beep.domain.attendance.controller.dto.response

data class AttendanceSortModeResponse(
    val grade: Int,
    val type: AttendanceTypeResponse?
)
