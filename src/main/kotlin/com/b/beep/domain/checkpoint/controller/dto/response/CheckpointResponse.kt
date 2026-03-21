package com.b.beep.domain.checkpoint.controller.dto.response

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import java.time.DayOfWeek
import java.time.LocalTime

data class CheckpointResponse(
    val id: Long,
    val name: String,
    val startAt: LocalTime,
    val endAt: LocalTime,
    val attendanceStartAt: LocalTime?,
    val attendanceEndAt: LocalTime?,
    val dayOfWeek: DayOfWeek?,
    val grade: Int?
) {
    companion object {
        fun of(entity: AttendanceCheckpointEntity): CheckpointResponse {
            return CheckpointResponse(
                id = entity.id!!,
                name = entity.name,
                startAt = entity.startAt,
                endAt = entity.endAt,
                attendanceStartAt = entity.attendanceStartAt,
                attendanceEndAt = entity.attendanceEndAt,
                dayOfWeek = entity.dayOfWeek,
                grade = entity.grade
            )
        }
    }
}
