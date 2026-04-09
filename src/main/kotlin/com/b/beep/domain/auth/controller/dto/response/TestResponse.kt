package com.b.beep.domain.auth.controller.dto.response

data class TestResponse(
    val name: String,
    val username: String,
    val profileImage: String?,
    val role: String?
)
