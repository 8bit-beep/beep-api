package com.b.beep.domain.notification.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class SaveTokenRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val token: String,

    @field:NotBlank(message = "디바이스 정보는 필수입니다")
    val device: String
)
