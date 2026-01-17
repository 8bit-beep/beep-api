package com.b.beep.domain.attendance.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AttendanceError(override val status: HttpStatus, override val message: String) : CustomError {
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "출석 기록을 찾을 수 없습니다."),
    ALREADY_ATTENDED(HttpStatus.BAD_REQUEST, "이미 출석처리 되었습니다."),
    ROOM_MISMATCH(HttpStatus.BAD_REQUEST, "출석하려는 실과 등록된 스케줄이 다릅니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "출석하려는 타입과 등록된 스케줄이 다릅니다."),
    NOT_EXISTS_ATTEND_TYPE(HttpStatus.NOT_FOUND, "출석하려는 타입이 등록되지 않았습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 교시에 스케줄이 등록되지 않았습니다."),
    TIME_UNAVAILABLE(HttpStatus.BAD_REQUEST, "출석할 수 없는 시간입니다."),
}
