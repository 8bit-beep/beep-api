package com.b.beep.domain.absence.controller.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CheckpointSetting(
    @field:NotNull(message = "체크포인트 ID는 필수입니다")
    @field:Positive(message = "체크포인트 ID는 양수여야 합니다")
    val checkpointId: Long,

    @field:Positive(message = "출석 타입 ID는 양수여야 합니다")
    val typeId: Long? = null
)
