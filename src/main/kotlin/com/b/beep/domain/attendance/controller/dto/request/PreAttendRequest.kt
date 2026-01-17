package com.b.beep.domain.attendance.controller.dto.request

data class PreAttendRequest(
    val grade: Int,
    val classNumber: Int,
    val num: Int,
    val statusId: Long,
    val checkpointId: Long
)
