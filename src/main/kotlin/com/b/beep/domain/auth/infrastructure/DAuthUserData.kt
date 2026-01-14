package com.b.beep.domain.auth.infrastructure


data class DAuthUserData(
    val sub: String,
    val grade: Int,
    val room: Int,
    val number: Int,
    val name: String,
    val profileImage: String?,
    val role: String,
    val email: String
)
