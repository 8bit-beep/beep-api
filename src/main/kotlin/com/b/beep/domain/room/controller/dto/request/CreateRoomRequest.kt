package com.b.beep.domain.room.controller.dto.request

data class CreateRoomRequest(
    val name: String,
    val grade: Long? = null,
    val classNumber: Long? = null,
)
