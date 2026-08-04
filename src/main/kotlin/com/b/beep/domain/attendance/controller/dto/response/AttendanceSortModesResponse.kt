package com.b.beep.domain.attendance.controller.dto.response

import java.time.LocalDate

data class AttendanceSortModesResponse(
    val date: LocalDate,
    val modes: List<AttendanceSortModeResponse>
)
