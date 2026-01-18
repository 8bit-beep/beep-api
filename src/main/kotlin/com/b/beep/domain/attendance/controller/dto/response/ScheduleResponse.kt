package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import java.time.DayOfWeek

data class ScheduleResponse(
    val id: Long,
    val dayOfWeek: DayOfWeek,
    val checkpoint: CheckpointSimpleResponse,
    val type: AttendanceTypeResponse,
    val room: RoomResponse
) {
    companion object {
        fun of(entity: StudentScheduleEntity): ScheduleResponse {
            return ScheduleResponse(
                id = entity.id!!,
                dayOfWeek = entity.dayOfWeek,
                checkpoint = CheckpointSimpleResponse.of(entity.checkpoint),
                type = AttendanceTypeResponse.of(entity.type),
                room = RoomResponse.of(entity.room)
            )
        }
    }
}
