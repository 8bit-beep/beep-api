package com.b.beep.domain.history.controller.dto.response

data class RoomAttendanceHistoryResponse(
    val room: String,
    val students: List<StudentAttendanceRecord>
)
