package com.b.beep.domain.attendance.controller.dto.response

data class AttendanceStudentResponse(
    val userId: Long,
    val name: String,
    val username: String,
    val studentId: String,
    val statuses: List<StatusResponse>
)
