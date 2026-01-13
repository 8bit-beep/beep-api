package com.b.beep.domain.memo.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class MemoError(override val status: HttpStatus, override val message: String) : CustomError {
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다.")
}
