package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CreateAttendanceRequest(
    @field:NotNull(message = "실 ID는 필수입니다")
    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long,

    @field:NotNull(message = "출석 유형 ID는 필수입니다")
    @field:Positive(message = "출석 유형 ID는 양수여야 합니다")
    val typeId: Long
)
