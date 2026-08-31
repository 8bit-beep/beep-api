package com.b.beep.domain.absence.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class OutSleepingOpenApiError(
    override val status: HttpStatus,
    override val message: String,
) : CustomError {
    DATE_REQUIRED(HttpStatus.BAD_REQUEST, "검색할 날짜는 필수입니다."),
}
