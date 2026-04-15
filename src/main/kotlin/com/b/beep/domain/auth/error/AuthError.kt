package com.b.beep.domain.auth.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AuthError(override val status: HttpStatus, override val message: String) : CustomError {
    TOKEN_FETCH_FAILED(HttpStatus.CONFLICT, "dauth 토큰을 불러오는 데 실패했습니다."),
    INVALID_DAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 DAuth 토큰입니다."),
    DAUTH_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "DAuth 서버 통신 중 오류가 발생했습니다."),
    NULL_EMAIL(HttpStatus.UNAUTHORIZED, "이메일 값이 null 입니다"),
    NULL_NAME(HttpStatus.UNAUTHORIZED, "이름 값이 null 입니다"),
    NULL_STU_NUM(HttpStatus.UNAUTHORIZED, "학번이 넘어오지 않았습니다"),
    NULL_ROLE(HttpStatus.UNAUTHORIZED, "role이 null 입니다."),
    DAUTH_LOGIN(HttpStatus.UNAUTHORIZED, "로그인 해주세요"),
    OAUTH_ONLY_ACCOUNT(HttpStatus.UNAUTHORIZED, "OAuth 전용 계정입니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.")
}
