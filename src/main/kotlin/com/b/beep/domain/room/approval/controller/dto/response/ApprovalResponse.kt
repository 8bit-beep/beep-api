package com.b.beep.domain.room.approval.controller.dto.response

import com.b.beep.domain.room.approval.entity.ApprovalEntity
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
        fun from(entity: ApprovalEntity): ApprovalResponse {
            return ApprovalResponse(
                room = RoomResponse.from(entity.room),
                period = entity.period,
                approvedTeacher = entity.teacher?.let { UserResponse.of(it) },
                approvedAt = entity.updatedAt!!
            )
        }
    }
}
