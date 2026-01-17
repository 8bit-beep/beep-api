package com.b.beep.domain.checkpoint.controller.dto.request

import jakarta.validation.constraints.Size
import java.time.LocalTime

data class UpdateCheckpointRequest(
    @field:Size(max = 50, message = "이름은 50자 이하여야 합니다")
    val name: String?,

    val startAt: LocalTime?,
    val endAt: LocalTime?,
    val attendanceStartAt: LocalTime?,
    val attendanceEndAt: LocalTime?
)
