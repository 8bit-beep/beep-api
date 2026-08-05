package com.b.beep.domain.auth.repository

interface RefreshTokenRepository {
    fun save(refreshToken: String, username: String)
    fun consume(refreshToken: String): String?
}
