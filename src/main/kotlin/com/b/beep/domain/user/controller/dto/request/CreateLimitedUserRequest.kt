package com.b.beep.domain.user.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateLimitedUserRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    val username: String
)
