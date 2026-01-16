package com.b.beep.domain.attendance.controller.dto.response.history

import com.b.beep.domain.attendance.controller.dto.response.history.StudentAttendanceRecord

data class ClassAttendanceHistoryResponse(
    val classification: String,
    val students: List<StudentAttendanceRecord>
)
