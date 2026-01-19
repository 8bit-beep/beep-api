package com.b.beep.domain.checkpoint.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class CheckpointError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    CHECKPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "체크포인트를 찾을 수 없습니다."),
    CHECKPOINT_TIME_OVERLAP(HttpStatus.CONFLICT, "기존 체크포인트와 시간이 겹칩니다."),
    CHECKPOINT_IN_USE(HttpStatus.CONFLICT, "사용 중인 체크포인트는 삭제할 수 없습니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "시작 시간이 종료 시간보다 늦을 수 없습니다.")
}
