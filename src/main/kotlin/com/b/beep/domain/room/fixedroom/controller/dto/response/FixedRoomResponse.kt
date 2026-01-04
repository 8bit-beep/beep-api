package com.b.beep.domain.room.fixedroom.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.fixedroom.entity.FixedRoomEntity

data class FixedRoomResponse(
    val id: Long,
    val room: RoomResponse,
    val type: AttendanceType
) {
    companion object {
        fun from(entity: FixedRoomEntity): FixedRoomResponse {
            return FixedRoomResponse(
                id = entity.id!!,
                room = RoomResponse.from(entity.room),
                type = entity.type
            )
        }
    }
}
