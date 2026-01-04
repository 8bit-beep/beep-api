package com.b.beep.domain.room.approval.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class ApprovalError(override val status: HttpStatus, override val message: String) : CustomError {
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "승인 정보를 찾을 수 없습니다."),
}
