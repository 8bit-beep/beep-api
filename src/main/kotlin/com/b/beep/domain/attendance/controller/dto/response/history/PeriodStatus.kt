package com.b.beep.domain.attendance.controller.dto.response.history

data class PeriodStatus(
    val checkpointId: Long,
    val checkpointName: String,
    val status: String
)