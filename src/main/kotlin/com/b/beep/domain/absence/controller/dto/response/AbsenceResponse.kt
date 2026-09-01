package com.b.beep.domain.absence.controller.dto.response

import java.time.LocalDate

data class AbsenceResponse(
    val absenceId: Long?,
    val source: AbsenceSource,
    val isGrouped: Boolean,
    val targetStudents: List<AbsenceStudentResponse>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val checkpoints: List<AbsenceExceptionResponse>,
    val reason: String,
    val typeId: Long,
    val typeName: String
)
