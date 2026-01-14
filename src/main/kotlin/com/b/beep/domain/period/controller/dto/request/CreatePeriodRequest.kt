package com.b.beep.domain.period.controller.dto.request

import java.time.LocalTime

data class CreatePeriodRequest(
    val period: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val attendStartTime: LocalTime,
    val attendEndTime: LocalTime
)
