package com.b.beep.domain.attendance.controller.dto.response.history

data class StudentAttendanceRecord(
    val username: String,
    val studentId: String,
    val statuses: List<PeriodStatus>
)