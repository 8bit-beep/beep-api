package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.absence.domain.entity.AbsenceExceptionEntity
import java.time.LocalDate

data class AbsenceExceptionResponse(
    val checkpointId: Long,
    val checkpointName: String,
    val date: LocalDate
) {
    companion object {
        fun of(entity: AbsenceExceptionEntity): AbsenceExceptionResponse {
            return AbsenceExceptionResponse(
                checkpointId = entity.checkpoint.id!!,
                checkpointName = entity.checkpoint.name,
                date = entity.date
            )
        }
    }
}
