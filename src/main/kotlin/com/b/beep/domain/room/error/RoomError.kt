package com.b.beep.domain.room.error

import com.b.beep.global.exception.CustomError
import org.springframework.http.HttpStatus

enum class RoomError(
    override val status: HttpStatus,
    override val message: String
) : CustomError {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Room not found"),
    ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "Room already exists")
}
