package com.b.beep.domain.checkpoint.controller.dto.response

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity

data class CheckpointSimpleResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(entity: AttendanceCheckpointEntity) = CheckpointSimpleResponse(
            id = entity.id!!,
            name = entity.name
        )
    }
}
