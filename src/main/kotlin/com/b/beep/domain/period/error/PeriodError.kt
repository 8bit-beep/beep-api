package com.b.beep.domain.period.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class PeriodError(override val status: HttpStatus, override val message: String) : CustomError {
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "교시 정보를 찾을 수 없습니다."),
    TIME_UNAVAILABLE(HttpStatus.BAD_REQUEST, "출석 가능 시간이 아닙니다."),
}
