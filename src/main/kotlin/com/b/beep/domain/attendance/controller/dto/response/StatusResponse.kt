package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType

data class StatusResponse(
    val checkpointId: Long,
    val checkpointName: String,
    val status: AttendanceType
)
