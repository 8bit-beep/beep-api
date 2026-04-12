package com.b.beep.domain.auth.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "인증 코드는 필수입니다")
    val code: String,

    @field:NotBlank(message = "code_verifier는 필수입니다")
    val codeVerifier: String,
)
