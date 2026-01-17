package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
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
        fun of(schedule: StudentScheduleEntity): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id!!,
                dayOfWeek = schedule.dayOfWeek,
                checkpoint = CheckpointSimpleResponse.of(schedule.checkpoint),
                type = AttendanceTypeResponse.of(schedule.type),
                room = RoomResponse.of(schedule.room)
            )
        }
    }
}
