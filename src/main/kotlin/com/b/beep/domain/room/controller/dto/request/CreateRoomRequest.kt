package com.b.beep.domain.room.controller.dto.request

data class CreateRoomRequest(
    val name: String,
    val grade: Int? = null,
    val classNumber: Int? = null,
    val floor: Int? = null
)
