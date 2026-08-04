package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse

data class AttendanceSortModeResponse(
    val grade: Int,
    val checkpoint: CheckpointSimpleResponse,
    val type: AttendanceTypeResponse?
)
