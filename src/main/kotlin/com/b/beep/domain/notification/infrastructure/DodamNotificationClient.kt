package com.b.beep.domain.notification.infrastructure

import com.b.beep.domain.auth.infrastructure.DodamMiniAppProperties
import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component
class DodamNotificationClient(
    private val properties: DodamMiniAppProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                    .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
            )
        )
        .build()

    fun send(
        title: String,
        body: String,
        targetUserPublicIds: List<String>,
    ): DodamNotificationResult? {
        if (properties.appPublicId.isBlank()) {
            logger.error("Dodam notification skipped: DODAM_MINI_APP_PUBLIC_ID is not configured")
            return null
        }

        val response = webClient.post()
            .uri(SEND_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                DodamNotificationRequest(
                    appPublicId = properties.appPublicId,
                    title = title,
                    body = body,
                    targetUserPublicIds = targetUserPublicIds,
                )
            )
            .retrieve()
            .bodyToMono(DodamNotificationResponse::class.java)
            .block()

        return response?.data
    }

    companion object {
        private const val SEND_PATH = "/notification/send"
        private const val CONNECT_TIMEOUT_MILLIS = 3_000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
    }
}
