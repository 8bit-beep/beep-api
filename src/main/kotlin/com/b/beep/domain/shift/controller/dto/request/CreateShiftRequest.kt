package com.b.beep.domain.shift.controller.dto.request

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateShiftRequest(
    @field:NotNull(message = "실 ID는 필수입니다")
    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long,

    @field:NotBlank(message = "사유는 필수입니다")
    @field:Size(max = 300, message = "사유는 300자 이하여야 합니다")
    val reason: String,

    @field:NotNull(message = "체크포인트 ID는 필수입니다")
    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long,

    @field:NotNull(message = "날짜는 필수입니다")
    @field:FutureOrPresent(message = "날짜는 오늘 이후여야 합니다")
    val date: LocalDate
)
