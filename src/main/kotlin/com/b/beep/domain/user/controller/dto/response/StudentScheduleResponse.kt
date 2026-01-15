package com.b.beep.domain.user.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.user.domain.enums.entity.StudentScheduleEntity
import java.time.DayOfWeek

data class StudentScheduleResponse(
    val id: Long,
    val dayOfWeek: DayOfWeek,
    val period: Int,
    val type: AttendanceType,
    val room: Room
) {
    companion object {
        fun of(schedule: StudentScheduleEntity): StudentScheduleResponse {
            return StudentScheduleResponse(
                id = schedule.id!!,
                dayOfWeek = schedule.dayOfWeek,
                period = schedule.period,
                type = schedule.type,
                room = schedule.room
            )
        }
    }
}
