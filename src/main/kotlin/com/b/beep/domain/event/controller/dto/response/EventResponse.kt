package com.b.beep.domain.event.controller.dto.response

import com.b.beep.domain.event.domain.entity.EventEntity
import java.time.LocalDate

data class EventResponse(
    val id: Long,
    val name: String,
    val date: LocalDate,
    val checkpointNames: List<String>,
    val studentCount: Int,
    val createdByName: String
) {
    companion object {
        fun of(
            entity: EventEntity,
            checkpointNames: List<String>,
            studentCount: Int
        ) = EventResponse(
            id = entity.id!!,
            name = entity.name,
            date = entity.date,
            checkpointNames = checkpointNames,
            studentCount = studentCount,
            createdByName = entity.createdBy.name
        )
    }
}
