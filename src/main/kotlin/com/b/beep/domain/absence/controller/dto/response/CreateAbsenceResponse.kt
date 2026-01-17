package com.b.beep.domain.absence.controller.dto.response

data class CreateAbsenceResponse(
    val absenceId: Long?,
    val skippedUserIds: List<Long>
)
