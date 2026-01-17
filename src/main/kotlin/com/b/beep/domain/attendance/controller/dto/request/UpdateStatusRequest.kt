package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class UpdateStatusRequest(
    @field:Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @field:Max(value = 3, message = "학년은 3 이하여야 합니다")
    val grade: Int,

    @field:Min(value = 1, message = "반은 1 이상이어야 합니다")
    val classNumber: Int,

    @field:Min(value = 1, message = "번호는 1 이상이어야 합니다")
    val num: Int,

    @field:NotNull(message = "상태 ID는 필수입니다")
    @field:Positive(message = "상태 ID는 양수여야 합니다")
    val statusId: Long,

    val date: LocalDate? = null,
    val checkpointId: Long? = null
)
