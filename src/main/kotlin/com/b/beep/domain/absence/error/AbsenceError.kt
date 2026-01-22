package com.b.beep.domain.absence.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AbsenceError(override val status: HttpStatus, override val message: String) : CustomError {
    ABSENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없는 장기결석입니다."),
    ABSENCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 학생은 이미 해당 날짜에 장기결석이 등록되어 있습니다."),
    START_DATE_IN_PAST(HttpStatus.BAD_REQUEST, "시작 날짜는 오늘 이후여야 합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작 날짜는 종료 날짜보다 이전이어야 합니다."),
    DATE_OUT_OF_ABSENCE_RANGE(HttpStatus.BAD_REQUEST, "제외할 출석 시간의 날짜는 결석 기간 내에 있어야 합니다.")
}
