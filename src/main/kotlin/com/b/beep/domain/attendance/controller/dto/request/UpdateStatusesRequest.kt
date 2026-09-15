package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class UpdateStatusesRequest(
    @field:NotEmpty(message = "변경할 학생 목록은 필수입니다")
    val userIds: List<@Positive(message = "유저 ID는 양수여야 합니다") Long>,

    @field:NotNull(message = "출석 타입 ID는 필수입니다")
    @field:Positive(message = "출석 타입 ID는 양수여야 합니다")
    val typeId: Long,

    val date: LocalDate? = null,

    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long? = null
)
