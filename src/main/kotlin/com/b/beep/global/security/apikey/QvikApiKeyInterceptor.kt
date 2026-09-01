package com.b.beep.global.security.apikey

import com.b.beep.global.exception.CustomException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class QvikApiKeyInterceptor(
    private val properties: QvikOpenApiProperties,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val providedApiKey = request.getHeader(HEADER_NAME)
        if (!matches(properties.apiKey, providedApiKey)) {
            throw CustomException(QvikApiKeyError.INVALID_QVIK_API_KEY)
        }
        return true
    }

    private fun matches(expected: String, actual: String?): Boolean {
        if (actual == null) return false
        return MessageDigest.isEqual(
            expected.toByteArray(StandardCharsets.UTF_8),
            actual.toByteArray(StandardCharsets.UTF_8),
        )
    }

    companion object {
        const val PATH = "/out-sleeping/openapi/search"
        const val HEADER_NAME = "X-Qvik-Api-Key"
    }
}
