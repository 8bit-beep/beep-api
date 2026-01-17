package com.b.beep.domain.attendance.controller.dto.response

data class AttendanceStudentResponse(
    val username: String,
    val studentId: String,
    val statuses: List<StatusResponse>
)
