package com.b.beep.domain.event.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class EventError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "행사를 찾을 수 없습니다."),
    EMPTY_CHECKPOINTS(HttpStatus.BAD_REQUEST, "교시를 하나 이상 선택해야 합니다."),
    EMPTY_USERS(HttpStatus.BAD_REQUEST, "학생을 한 명 이상 선택해야 합니다."),
}
