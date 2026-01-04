package com.b.beep.domain.period.controller.dto.request

import java.time.LocalTime

data class CreatePeriodRequest(
    val period: Int,
    val attendanceStartTime: LocalTime,
    val attendanceEndTime: LocalTime,
    val periodStartTime: LocalTime,
    val periodEndTime: LocalTime,
)
