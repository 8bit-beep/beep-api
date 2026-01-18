package com.b.beep.domain.user.controller.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.DayOfWeek

data class CreateStudentScheduleRequest(
    @field:NotNull(message = "유저 ID는 필수입니다")
    @field:Positive(message = "유저 ID는 양수여야 합니다")
    val userId: Long,

    @field:NotNull(message = "요일은 필수입니다")
    val dayOfWeek: DayOfWeek,

    @field:NotNull(message = "체크포인트 ID는 필수입니다")
    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long,

    @field:NotNull(message = "스케줄 유형 ID는 필수입니다")
    @field:Positive(message = "스케줄 유형 ID는 양수여야 합니다")
    val typeId: Long,

    @field:NotNull(message = "실 ID는 필수입니다")
    @field:Positive(message = "실 ID는 양수여야 합니다")
    val roomId: Long
)
