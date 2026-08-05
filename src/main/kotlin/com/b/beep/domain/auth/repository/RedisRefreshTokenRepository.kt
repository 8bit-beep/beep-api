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

    override fun save(refreshToken: String, username: String) {
        redisTemplate.opsForValue().set(
            prefix + refreshToken,
            username,
            Duration.ofMillis(jwtProperties.refreshExp)
        )
    }

    override fun consume(refreshToken: String): String? {
        return redisTemplate.opsForValue().getAndDelete(prefix + refreshToken)
    }
}
