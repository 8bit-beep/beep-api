package com.b.beep.domain.attendance.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AttendanceTypeError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    ATTENDANCE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "출석 유형을 찾을 수 없습니다."),
    ATTENDANCE_TYPE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 출석 유형입니다.")
}
