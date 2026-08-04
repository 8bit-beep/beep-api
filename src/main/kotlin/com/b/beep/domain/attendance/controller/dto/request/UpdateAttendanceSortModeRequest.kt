package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive

data class UpdateAttendanceSortModeRequest(
    @field:Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @field:Max(value = 3, message = "학년은 3 이하여야 합니다")
    val grade: Int,

    @field:Positive(message = "출석 유형 ID는 양수여야 합니다")
    val typeId: Long?
)
