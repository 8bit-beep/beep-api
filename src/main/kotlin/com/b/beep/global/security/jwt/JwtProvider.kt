package com.b.beep.global.security.jwt

import com.b.beep.global.security.jwt.config.JwtProperties
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import com.b.beep.global.security.jwt.enums.JwtType
import com.b.beep.domain.auth.repository.RefreshTokenRepository
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(username: String): TokenResponse {
        val accessToken = generateAccessToken(username)
        val refreshToken = generateRefreshToken(username)
        refreshTokenRepository.save(refreshToken, username)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun generateAccessToken(username: String): String {
        val now = Date()

        return Jwts.builder()
            .setHeaderParam(Header.JWT_TYPE, JwtType.ACCESS)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + jwtProperties.accessExp))
            .signWith(getSigningKey())
            .compact()
    }

    fun generateRefreshToken(username: String): String {
        val now = Date()
        return Jwts.builder()
            .setHeaderParam(Header.JWT_TYPE, JwtType.REFRESH)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + jwtProperties.refreshExp))
            .signWith(getSigningKey())
            .compact()
    }
}
