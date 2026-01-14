package com.b.beep.domain.period.controller.dto.request

import java.time.LocalTime

data class UpdatePeriodRequest(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val attendStartTime: LocalTime,
    val attendEndTime: LocalTime
)
