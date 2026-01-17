package com.b.beep.domain.absence.controller.dto.response

data class UpdateAbsenceResponse(
    val absenceId: Long,
    val skippedUserIds: List<Long>
)
