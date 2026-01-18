package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class UpdateStatusRequest(
    @field:NotNull(message = "유저 ID는 필수입니다")
    @field:Positive(message = "유저 ID는 양수여야 합니다")
    val userId: Long,

    @field:NotNull(message = "상태 ID는 필수입니다")
    @field:Positive(message = "상태 ID는 양수여야 합니다")
    val statusId: Long,

    val date: LocalDate? = null,
    val checkpointId: Long? = null
)
