package com.b.beep.domain.history.controller.dto.response

data class StudentAttendanceRecord(
    val studentNumber: String,
    val studentName: String,
    val period1: String,
    val period2: String,
    val period3: String,
    val finalStatus: String
)
