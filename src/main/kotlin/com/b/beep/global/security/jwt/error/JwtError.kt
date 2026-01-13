package com.b.beep.global.security.jwt.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class JwtError(override val status: HttpStatus, override val message: String) : CustomError {
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 타입입니다."),
    INCORRECT_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "존제하지 않는 리프레쉬 토큰입니다. 로그인 해주세요 "),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레쉬 토큰이 저장된 토큰과 다릅니다."),
}