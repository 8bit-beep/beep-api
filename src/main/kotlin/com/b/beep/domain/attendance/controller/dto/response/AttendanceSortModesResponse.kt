package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import java.time.LocalDate

data class AttendanceSortModesResponse(
    val date: LocalDate,
    val checkpoint: CheckpointSimpleResponse,
    val modes: List<AttendanceSortModeResponse>
)
