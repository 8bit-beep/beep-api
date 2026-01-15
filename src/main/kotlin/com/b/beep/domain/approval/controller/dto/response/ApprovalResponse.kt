package com.b.beep.domain.approval.controller.dto.response

import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.user.controller.dto.response.UserResponse
import java.time.LocalDateTime

data class ApprovalResponse(
    val room: RoomResponse,
    val period: Int,
    val approvedTeacher: UserResponse? = null,
    val approvedAt: LocalDateTime,
) {
    companion object {
        fun of(approval: ApprovalEntity): ApprovalResponse {
            return ApprovalResponse(
                room = RoomResponse.of(approval.room),
                period = approval.period,
                approvedTeacher = approval.teacher?.let { UserResponse.of(it) },
                approvedAt = approval.updatedAt!!
            )
        }
    }
}
