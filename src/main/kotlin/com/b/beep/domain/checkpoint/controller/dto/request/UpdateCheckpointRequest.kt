package com.b.beep.domain.checkpoint.controller.dto.request

import java.time.LocalTime

data class UpdateCheckpointRequest(
    val name: String?,
    val startAt: LocalTime?,
    val endAt: LocalTime?,
    val attendanceStartAt: LocalTime?,
    val attendanceEndAt: LocalTime?
)
