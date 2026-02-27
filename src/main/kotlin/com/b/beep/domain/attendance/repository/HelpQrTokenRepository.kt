package com.b.beep.domain.attendance.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.UUID

@Repository
class HelpQrTokenRepository(
    private val redisTemplate: StringRedisTemplate
) {
    private val prefix = "help-qr:"
    private val ttl = Duration.ofMinutes(1)

    fun save(helperId: Long, checkpointId: Long, roomId: Long, typeId: Long): String {
        val token = UUID.randomUUID().toString()
        val value = "$helperId:$checkpointId:$roomId:$typeId"
        redisTemplate.opsForValue().set(prefix + token, value, ttl)
        return token
    }

    fun findByToken(token: String): HelpQrTokenData? {
        val value = redisTemplate.opsForValue().get(prefix + token) ?: return null
        val parts = value.split(":")
        if (parts.size != 4) return null
        return HelpQrTokenData(
            helperId = parts[0].toLong(),
            checkpointId = parts[1].toLong(),
            roomId = parts[2].toLong(),
            typeId = parts[3].toLong()
        )
    }

    fun delete(token: String) {
        redisTemplate.delete(prefix + token)
    }
}

data class HelpQrTokenData(
    val helperId: Long,
    val checkpointId: Long,
    val roomId: Long,
    val typeId: Long
)