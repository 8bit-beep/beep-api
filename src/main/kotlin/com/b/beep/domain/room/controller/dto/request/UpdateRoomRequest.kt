package com.b.beep.domain.room.controller.dto.request

import jakarta.validation.constraints.Size

data class UpdateRoomRequest(
    @field:Size(max = 100, message = "실 이름은 100자 이하여야 합니다")
    val name: String,

    val grade: Int? = null,
    val classNumber: Int? = null,
    val floor: Int? = null
)
