package com.b.beep.domain.memo.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateMemoRequest(
    @field:NotBlank(message = "메모 내용은 필수입니다")
    @field:Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
    val content: String,
)
