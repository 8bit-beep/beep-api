package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ScanHelpQrRequest(
    @field:NotBlank(message = "토큰은 필수입니다.")
    val token: String,
    @field:Positive
    val typeId: Long
)