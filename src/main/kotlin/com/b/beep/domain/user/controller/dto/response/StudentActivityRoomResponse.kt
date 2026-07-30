package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import java.time.DayOfWeek

data class StudentActivityRoomResponse(
    val id: Long,
    val dayOfWeek: DayOfWeek,
    val type: AttendanceTypeResponse,
    val room: RoomResponse
) {
    companion object {
        fun of(entity: StudentActivityRoomEntity): StudentActivityRoomResponse {
            return StudentActivityRoomResponse(
                id = entity.id!!,
                dayOfWeek = entity.dayOfWeek,
                type = AttendanceTypeResponse.of(entity.type),
                room = RoomResponse.of(entity.room)
            )
        }
    }
}
