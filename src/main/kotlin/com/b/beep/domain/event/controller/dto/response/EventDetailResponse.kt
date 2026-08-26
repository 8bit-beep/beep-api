package com.b.beep.domain.event.controller.dto.response

import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.event.domain.entity.EventEntity
import java.time.LocalDate

data class EventDetailResponse(
    val id: Long,
    val name: String,
    val date: LocalDate,
    val checkpoints: List<CheckpointSimpleResponse>,
    val students: List<EventStudentResponse>,
    val createdByName: String
) {
    companion object {
        fun of(
            entity: EventEntity,
            checkpoints: List<CheckpointSimpleResponse>,
            students: List<EventStudentResponse>
        ) = EventDetailResponse(
            id = entity.id!!,
            name = entity.name,
            date = entity.date,
            checkpoints = checkpoints,
            students = students,
            createdByName = entity.createdBy.name
        )
    }
}

data class EventStudentResponse(
    val userId: Long,
    val studentId: String,
    val name: String
)
