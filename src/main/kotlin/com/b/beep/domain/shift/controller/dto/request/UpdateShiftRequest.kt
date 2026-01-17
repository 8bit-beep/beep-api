package com.b.beep.domain.shift.controller.dto.request

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UpdateShiftRequest(
    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long?,

    @field:Size(max = 300, message = "사유는 300자 이하여야 합니다")
    val reason: String?,

    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long?,

    @field:FutureOrPresent(message = "날짜는 오늘 이후여야 합니다")
    val date: LocalDate?,
)
