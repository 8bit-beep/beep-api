package com.b.beep.domain.history.controller.dto.response

data class StudentAttendanceRecord(
    val username: String,
    val studentId: String,
    val statuses: List<PeriodStatus>
)
