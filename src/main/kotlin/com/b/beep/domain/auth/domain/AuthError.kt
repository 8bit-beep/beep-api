package com.b.beep.domain.auth.domain

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AuthError(override val status: HttpStatus, override val message: String) : CustomError {
    TOKEN_FETCH_FAILED(HttpStatus.CONFLICT, "dauth 토큰을 불러오는 데 실패했습니다."),
    NULL_EMAIL(HttpStatus.UNAUTHORIZED, "이메일 값이 null 입니다"),
    NULL_NAME(HttpStatus.UNAUTHORIZED, "이름 값이 null 입니다"),
    DAUTH_LOGIN(HttpStatus.UNAUTHORIZED, "로그인해 주세요")

}
