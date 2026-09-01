package com.b.beep.global.security.apikey

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class QvikApiKeyError(
    override val status: HttpStatus,
    override val message: String,
) : CustomError {
    INVALID_QVIK_API_KEY(HttpStatus.UNAUTHORIZED, "유효하지 않은 큐빅 API 키입니다."),
}
