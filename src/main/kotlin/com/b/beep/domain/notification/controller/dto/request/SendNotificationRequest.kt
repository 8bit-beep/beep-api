package com.b.beep.domain.notification.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SendNotificationRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 100, message = "제목은 100자 이하여야 합니다")
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다")
    @field:Size(max = 500, message = "내용은 500자 이하여야 합니다")
    val body: String,

    val imageUrl: String? = null,
)
