package com.b.beep.domain.auth.repository

interface RefreshTokenRepository {
    fun save(userId: String, refreshToken: String)
    fun findByUserId(userId: String): String?
    fun delete(userId: String)
}
