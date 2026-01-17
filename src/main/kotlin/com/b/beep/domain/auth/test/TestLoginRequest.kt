package com.b.beep.domain.auth.test

data class TestLoginRequest(
    val email: String,
    val name: String,
    val role: String,
    val grade: Int? = null,
    val classNumber: Int? = null,
    val num: Int? = null
)