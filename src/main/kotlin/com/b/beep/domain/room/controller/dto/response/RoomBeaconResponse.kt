package com.b.beep.domain.room.controller.dto.response

data class RoomBeaconResponse(
    val id: Long,
    val name: String,
    val grade: Int,
    val classNumber: Int,
    val beacon: BeaconResponse
)
