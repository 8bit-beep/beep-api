package com.b.beep.domain.period.controller.dto.request

import java.time.LocalTime

data class UpdatePeriodRequest(
    val attendanceStartTime: LocalTime? = null,
    val attendanceEndTime: LocalTime? = null,
    val periodStartTime: LocalTime? = null,
    val periodEndTime: LocalTime? = null,
)
