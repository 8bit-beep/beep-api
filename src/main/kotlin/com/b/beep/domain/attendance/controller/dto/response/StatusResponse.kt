package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse

data class StatusResponse(
    val checkpoint: CheckpointSimpleResponse,
    val status: AttendanceType
)
