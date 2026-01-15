package com.b.beep.domain.period.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class PeriodError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "교시 설정을 찾을 수 없습니다."),
    PERIOD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 교시입니다."),
    PERIOD_TIME_OVERLAP(HttpStatus.CONFLICT, "다른 교시와 시간이 겹칩니다.")
}
