package com.b.beep.domain.room.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class RoomError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "실을 찾을 수 없습니다."),
    ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 실입니다.")
}
