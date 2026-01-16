package com.b.beep.domain.history.controller.dto.response

data class ClassAttendanceHistoryResponse(
    val classification: String,
    val students: List<StudentAttendanceRecord>
)
