package com.b.beep.domain.user.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class LimitedUserError(override val status: HttpStatus, override val message: String) : CustomError {
    ALREADY_EXIST_LIMITED_USER(HttpStatus.BAD_REQUEST, "이미 제한된 계정입니다."),
    LIMITED_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "제한된 계정을 찾을 수 없습니다.")
}
