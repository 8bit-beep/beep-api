package com.b.beep.domain.auth.controller.dto.response

data class TestResponse(
    val name: String,
    val email: String,
    val profileImage: String?,
    val role: String?
)
