package com.b.beep.domain.checkpoint.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.DayOfWeek
import java.time.LocalTime

data class CreateCheckpointRequest(
    @field:NotBlank(message = "체크포인트 이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자 이하여야 합니다")
    val name: String,

    @field:NotNull(message = "시작 시간은 필수입니다")
    val startAt: LocalTime,

    @field:NotNull(message = "종료 시간은 필수입니다")
    val endAt: LocalTime,

    @field:NotNull(message = "출석 시작 시간은 필수입니다")
    val attendanceStartAt: LocalTime,

    @field:NotNull(message = "출석 종료 시간은 필수입니다")
    val attendanceEndAt: LocalTime,

    val dayOfWeek: DayOfWeek? = null,

    val grade: Int? = null
)
