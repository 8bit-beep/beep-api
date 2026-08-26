package com.b.beep.domain.event.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateEventRequest(
    @field:NotBlank(message = "행사명은 필수입니다")
    @field:Size(max = 100, message = "행사명은 100자 이하여야 합니다")
    val name: String,

    @field:NotNull(message = "날짜는 필수입니다")
    val date: LocalDate,

    @field:Size(min = 1, message = "교시를 하나 이상 선택해야 합니다")
    val checkpointIds: List<Long>,

    @field:Size(min = 1, message = "학생을 한 명 이상 선택해야 합니다")
    val userIds: List<Long>
)
