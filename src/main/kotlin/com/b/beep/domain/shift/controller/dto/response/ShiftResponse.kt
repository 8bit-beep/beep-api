package com.b.beep.domain.shift.controller.dto.response

import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.user.controller.dto.response.UserResponse
import java.time.LocalDate

data class ShiftResponse(
    val id: Long,
    val user: UserResponse,
    val room: RoomResponse,
    val checkpoint: CheckpointSimpleResponse,
    val reason: String,
    val status: ShiftStatus,
    val date: LocalDate,
)
