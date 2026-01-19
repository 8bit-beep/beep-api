package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.absence.domain.entity.AbsenceCheckpointEntity
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse

data class AbsenceCheckpointResponse(
    val checkpoint: CheckpointSimpleResponse,
    val type: AttendanceTypeResponse?
) {
    companion object {
        fun of(entity: AbsenceCheckpointEntity) = AbsenceCheckpointResponse(
            checkpoint = CheckpointSimpleResponse.of(entity.checkpoint),
            type = entity.type?.let { AttendanceTypeResponse.of(it) }
        )
    }
}
