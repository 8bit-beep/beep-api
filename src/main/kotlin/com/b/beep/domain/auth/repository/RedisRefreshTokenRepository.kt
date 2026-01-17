package com.b.beep.domain.auth.repository

import com.b.beep.global.security.jwt.config.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisRefreshTokenRepository(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties
) : RefreshTokenRepository {
    private val prefix = "refresh:"

    override fun save(email: String, refreshToken: String) {
        redisTemplate.opsForValue().set(
            prefix + email,
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshExp)
        )
    }

    override fun findByUserId(email: String): String? {
        return redisTemplate.opsForValue().get(prefix + email)
    }

    override fun delete(email: String) {
        redisTemplate.delete(prefix + email)
    }
}
