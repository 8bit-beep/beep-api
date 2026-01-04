package com.b.beep.domain.room.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class RoomError(override val status: HttpStatus, override val message: String) : CustomError {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없는 실입니다.")
}