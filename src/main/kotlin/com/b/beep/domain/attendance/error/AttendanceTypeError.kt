package com.b.beep.domain.attendance.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class AttendanceTypeError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    ATTENDANCE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "Attendance type not found"),
    ATTENDANCE_TYPE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Attendance type already exists")
}
