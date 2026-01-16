package com.b.beep.domain.attendance.controller.dto.response.history

import com.b.beep.domain.attendance.controller.dto.response.history.StudentAttendanceRecord

data class RoomAttendanceHistoryResponse(
    val room: String,
    val students: List<StudentAttendanceRecord>
)
