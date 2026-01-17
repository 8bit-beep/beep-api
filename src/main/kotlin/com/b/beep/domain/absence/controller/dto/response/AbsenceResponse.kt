package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import java.time.LocalDate

data class AbsenceResponse(
    val absenceId: Long,
    val isGrouped: Boolean,
    val targetStudents: List<AbsenceStudentResponse>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val checkpoints: List<CheckpointSimpleResponse>,
    val reason: String,
    val type: AttendanceTypeResponse?
)
