package com.b.beep.domain.room.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class RoomApprovalError(override val status: HttpStatus, override val message: String) : CustomError {
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "승인 정보를 찾을 수 없습니다."),
    ALREADY_APPROVED(HttpStatus.CONFLICT, "이미 승인된 실입니다."),
}