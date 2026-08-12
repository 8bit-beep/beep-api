package com.b.beep.domain.notification.infrastructure

import com.b.beep.domain.auth.infrastructure.DodamMiniAppProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

class DodamNotificationClientTest {
    private var server: HttpServer? = null
    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun tearDown() {
        server?.stop(0)
    }

    @Test
    fun `sends notification to Dodam API with Beep app public id`() {
        val capturedMethod = AtomicReference<String>()
        val capturedPath = AtomicReference<String>()
        val capturedBody = AtomicReference<String>()
        server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/notification/send") { exchange ->
                capturedMethod.set(exchange.requestMethod)
                capturedPath.set(exchange.requestURI.path)
                capturedBody.set(exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8))

                val response = """{"status":200,"message":"ok","data":{"successCount":2,"failureCount":1}}"""
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { it.write(response.toByteArray()) }
            }
            start()
        }

        val client = DodamNotificationClient(
            properties(baseUrl = "http://localhost:${server!!.address.port}")
        )

        val result = client.send(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            targetUserPublicIds = listOf("user-1", "user-2"),
        )

        assertThat(capturedMethod.get()).isEqualTo("POST")
        assertThat(capturedPath.get()).isEqualTo("/notification/send")
        val json = objectMapper.readTree(capturedBody.get())
        assertThat(json["appPublicId"].asText()).isEqualTo(APP_PUBLIC_ID)
        assertThat(json["title"].asText()).isEqualTo("출석 시간입니다!")
        assertThat(json["body"].asText()).isEqualTo("저녁 출석이 시작되었습니다.")
        assertThat(json["targetUserPublicIds"].map { it.asText() }).containsExactly("user-1", "user-2")
        assertThat(json["data"].isObject).isTrue()
        assertThat(json["data"].isEmpty).isTrue()
        assertThat(result).isEqualTo(DodamNotificationResult(successCount = 2, failureCount = 1))
    }

    @Test
    fun `skips notification when app public id is missing`() {
        val client = DodamNotificationClient(
            properties(baseUrl = "http://localhost:1", appPublicId = "")
        )

        val result = client.send("title", "body", listOf("user-1"))

        assertThat(result).isNull()
    }

    private fun properties(
        baseUrl: String,
        appPublicId: String = APP_PUBLIC_ID,
    ) = DodamMiniAppProperties(
        baseUrl = baseUrl,
        appPublicId = appPublicId,
    )

    companion object {
        private const val APP_PUBLIC_ID = "test-app-public-id"
    }
}
