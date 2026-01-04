package com.b.beep.domain.room.controller.dto.request

data class UpdateRoomRequest(
    val name: String? = null,
    val grade: Long? = null,
    val classNumber: Long? = null,
)