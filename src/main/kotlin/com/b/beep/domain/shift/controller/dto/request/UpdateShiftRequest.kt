package com.b.beep.domain.shift.controller.dto.request

import java.time.LocalDate

data class UpdateShiftRequest(
    val shiftId: Long,
    val roomId: Long?,
    val reason: String?,
    val period: Int?,
    val date: LocalDate?,
)
