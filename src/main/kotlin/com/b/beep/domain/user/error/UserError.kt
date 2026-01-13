package com.b.beep.domain.user.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class UserError(override val status: HttpStatus, override val message: String) : CustomError {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    STUDENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다."),
}