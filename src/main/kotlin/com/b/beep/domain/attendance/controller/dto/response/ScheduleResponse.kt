package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import java.time.DayOfWeek

data class ScheduleResponse(
    val id: Long,
    val dayOfWeek: DayOfWeek,
    val checkpointId: Long,
    val checkpointName: String,
    val type: AttendanceType,
    val room: RoomResponse
) {
    companion object {
        fun of(schedule: StudentScheduleEntity): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id!!,
                dayOfWeek = schedule.dayOfWeek,
                checkpointId = schedule.checkpoint.id!!,
                checkpointName = schedule.checkpoint.name,
                type = schedule.type,
                room = RoomResponse.of(schedule.room)
            )
        }
    }
}
