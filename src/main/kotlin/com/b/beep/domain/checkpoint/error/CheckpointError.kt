package com.b.beep.domain.checkpoint.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class CheckpointError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    CHECKPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "Checkpoint not found"),
    CHECKPOINT_TIME_OVERLAP(HttpStatus.CONFLICT, "Checkpoint time overlaps with existing checkpoint"),
    CHECKPOINT_IN_USE(HttpStatus.CONFLICT, "Checkpoint is in use and cannot be deleted")
}
