package com.b.beep.domain.notification.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DiscordWebhookService(
    @Value("\${discord.webhook-url}") private val webhookUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    fun send(message: String) {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val body = mapOf("content" to message)
        val request = HttpEntity(body, headers)

        try {
            restTemplate.postForEntity(webhookUrl, request, String::class.java)
            logger.info("Discord 알림 전송 완료")
        } catch (e: Exception) {
            logger.error("Discord 알림 전송 실패: ${e.message}")
        }
    }
}
