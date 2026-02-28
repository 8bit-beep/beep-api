package com.b.beep.domain.absence.controller.dto.request

import com.b.beep.domain.absence.domain.AbsenceReason
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate

data class CreateAbsenceRequest(
    @field:NotEmpty(message = "사용자 ID 목록은 필수입니다")
    val userIds: List<Long>,

    @field:NotNull(message = "시작일은 필수입니다")
    val startDate: LocalDate,

    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDate,

    @field:NotBlank(message = "사유는 필수입니다")
    @field:Size(max = 500, message = "사유는 500자 이하여야 합니다")
    val reason: String,

    @field:NotNull(message = "결석 사유 타입은 필수입니다")
    val absenceType: AbsenceReason,

    @field:Valid
    val checkpoints: List<AbsenceExceptionRequest>? = null
)
