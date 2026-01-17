package com.b.beep.domain.room.controller.dto.response

import com.b.beep.domain.user.controller.dto.response.UserResponse
import java.time.LocalDateTime

data class RoomApprovalResponse(
    val room: RoomResponse,
    val approved: Boolean,
    val approvedTeacher: RoomApprovedTeacherResponse? = null,
    val approvedAt: LocalDateTime? = null,
)
