package com.b.beep.domain.room.controller.dto.response

import com.b.beep.domain.room.domain.entity.RoomEntity

data class RoomResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(room: RoomEntity): RoomResponse {
            return RoomResponse(
                id = room.id!!,
                name = room.name
            )
        }
    }
}
