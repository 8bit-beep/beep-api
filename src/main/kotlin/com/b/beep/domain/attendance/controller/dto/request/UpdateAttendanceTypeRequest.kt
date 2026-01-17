package com.b.beep.domain.attendance.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class UpdateAttendanceTypeRequest(
    @field:NotBlank(message = "출석 유형 이름은 필수입니다")
    val name: String
)
