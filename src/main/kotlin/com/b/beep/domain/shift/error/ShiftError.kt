package com.b.beep.domain.shift.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class ShiftError(override val status: HttpStatus, override val message: String) : CustomError {
    SHIFT_NOT_FOUND(HttpStatus.NOT_FOUND, "실 이동 신청을 찾을 수 없습니다."),
    SHIFT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 교시에 이미 실이동 신청이 되어있습니다."),
    PASSED_TIME(HttpStatus.BAD_REQUEST, "이미 지난 교시는 신청할 수 없습니다."),
    SHIFT_ROOM_MISMATCH(HttpStatus.BAD_REQUEST, "실 이동 신청 실과 출석하려는 실이 다릅니다."),
}