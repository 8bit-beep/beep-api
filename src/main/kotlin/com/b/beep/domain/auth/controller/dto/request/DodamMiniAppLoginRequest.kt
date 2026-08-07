package com.b.beep.domain.auth.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class DodamMiniAppLoginRequest(
    @field:NotBlank(message = "도담 토큰은 필수입니다")
    val token: String,
)
