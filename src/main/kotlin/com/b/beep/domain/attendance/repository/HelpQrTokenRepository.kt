package com.b.beep.domain.attendance.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.UUID

@Repository
class HelpQrTokenRepository(
    private val redisTemplate: StringRedisTemplate
) {
    private val tokenPrefix = "help-qr:"
    private val userPrefix = "help-qr-user:"
    private val ttl = Duration.ofMinutes(1)

    fun save(helperId: Long, checkpointId: Long, roomId: Long): String {
        val userKey = "$userPrefix$helperId:$checkpointId"
        val existingToken = redisTemplate.opsForValue().get(userKey)
        if (existingToken != null) {
            redisTemplate.delete(tokenPrefix + existingToken)
        }

        val token = UUID.randomUUID().toString()
        val value = "$helperId:$checkpointId:$roomId"
        redisTemplate.opsForValue().set(tokenPrefix + token, value, ttl)
        redisTemplate.opsForValue().set(userKey, token, ttl)
        return token
    }

    fun findByToken(token: String): HelpQrTokenData? {
        val value = redisTemplate.opsForValue().get(tokenPrefix + token) ?: return null
        val parts = value.split(":")
        if (parts.size != 3) return null
        return HelpQrTokenData(
            helperId = parts[0].toLong(),
            checkpointId = parts[1].toLong(),
            roomId = parts[2].toLong()
        )
    }

    fun delete(token: String) {
        val tokenData = findByToken(token) ?: return
        redisTemplate.delete(tokenPrefix + token)
        redisTemplate.delete("$userPrefix${tokenData.helperId}:${tokenData.checkpointId}")
    }
}

data class HelpQrTokenData(
    val helperId: Long,
    val checkpointId: Long,
    val roomId: Long
)