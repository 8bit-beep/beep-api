package com.b.beep.domain.room.controller.dto.response

import com.b.beep.domain.room.entity.RoomEntity

data class RoomResponse(
    val id: Long,
    val name: String,
    val grade: Long? = null,
    val classNumber: Long? = null,
) {
    companion object {
        fun from(entity: RoomEntity): RoomResponse {
            return RoomResponse(
                id = entity.id!!,
                name = entity.name,
                grade = entity.grade,
                classNumber = entity.classNumber,
            )
        }
    }
}
