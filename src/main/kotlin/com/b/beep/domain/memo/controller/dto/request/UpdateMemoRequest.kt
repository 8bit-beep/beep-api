package com.b.beep.domain.memo.controller.dto.request

import jakarta.validation.constraints.Size

data class UpdateMemoRequest(
    @field:Size(max = 30000, message = "메모는 30000자 이하여야 합니다")
    val newContent: String
)
