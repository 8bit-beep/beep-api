package com.b.beep.domain.user.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class StudentScheduleError(override val status: HttpStatus, override val message: String) : CustomError {
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "스케줄을 찾을 수 없습니다."),
    ALREADY_EXIST_SCHEDULE(HttpStatus.BAD_REQUEST, "해당 시간대에 이미 스케줄이 존재합니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, "스케줄을 변경할 권한이 없습니다."),
    INVALID_DAY_OF_WEEK(HttpStatus.BAD_REQUEST, "월~목요일만 등록 가능합니다."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "1, 2, 3교시만 등록 가능합니다."),
}
