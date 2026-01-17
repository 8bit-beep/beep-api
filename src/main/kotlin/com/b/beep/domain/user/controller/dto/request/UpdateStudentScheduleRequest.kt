package com.b.beep.domain.user.controller.dto.request

import jakarta.validation.constraints.Positive
import java.time.DayOfWeek

data class UpdateStudentScheduleRequest(
    val dayOfWeek: DayOfWeek? = null,

    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long? = null,

    @field:Positive(message = "유형 ID는 양수여야 합니다")
    val typeId: Long? = null,

    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long? = null
)
