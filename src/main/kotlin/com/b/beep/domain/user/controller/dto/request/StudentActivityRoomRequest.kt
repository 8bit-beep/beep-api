package com.b.beep.domain.user.controller.dto.request

import jakarta.validation.constraints.Positive
import java.time.DayOfWeek

data class StudentActivityRoomRequest(
    val dayOfWeek: DayOfWeek?,

    @field:Positive(message = "출석 유형 ID는 양수여야 합니다")
    val typeId: Long,

    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long
)
