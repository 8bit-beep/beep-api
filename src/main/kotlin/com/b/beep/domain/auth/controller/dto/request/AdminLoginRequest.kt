package com.b.beep.domain.auth.controller.dto.request

data class AdminLoginRequest (
    val email: String,
    val password: String
)