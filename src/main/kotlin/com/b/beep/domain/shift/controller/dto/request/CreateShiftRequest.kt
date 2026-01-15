package com.b.beep.domain.shift.controller.dto.request

import java.time.LocalDate

data class CreateShiftRequest(
    val roomId: Long,
    val reason: String,
    val period: Int,
    val date: LocalDate
)
