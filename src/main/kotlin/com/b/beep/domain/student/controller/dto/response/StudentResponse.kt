package com.b.beep.domain.student.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.entity.StudentInfoEntity
import com.b.beep.domain.user.entity.StudentScheduleEntity
import com.b.beep.domain.attendance.entity.AttendanceEntity
import java.time.DayOfWeek

data class StudentResponse(
    val username: String,
    val studentId: String,
    val schedules: List<ScheduleResponse>,
    val statuses: List<StatusResponse>
) {
    companion object {
        fun of(
            user: UserEntity,
            studentInfo: StudentInfoEntity,
            schedules: List<StudentScheduleEntity>,
            attendances: List<AttendanceEntity>
        ): StudentResponse {
            return StudentResponse(
                username = user.username,
                studentId = String.format("%d%d%02d", studentInfo.grade, studentInfo.cls, studentInfo.num),
                schedules = schedules.map { ScheduleResponse.of(it) },
                statuses = attendances.map { StatusResponse(it.period, it.type) }
            )
        }
    }
}

data class ScheduleResponse(
    val id: Long,
    val dayOfWeek: DayOfWeek,
    val period: Int,
    val type: AttendanceType,
    val room: Room
) {
    companion object {
        fun of(schedule: StudentScheduleEntity): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id!!,
                dayOfWeek = schedule.dayOfWeek,
                period = schedule.period,
                type = schedule.type,
                room = schedule.room
            )
        }
    }
}

data class StatusResponse(
    val period: Int,
    val status: AttendanceType
)
