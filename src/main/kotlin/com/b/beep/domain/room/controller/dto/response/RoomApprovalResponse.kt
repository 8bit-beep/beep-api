package com.b.beep.domain.room.controller.dto.response

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.controller.dto.response.UserResponse
import java.time.LocalDateTime

data class RoomApprovalResponse(
    val room: RoomResponse,
    val checkpointId: Long,
    val checkpointName: String,
    val approved: Boolean,
    val approvedTeacher: UserResponse? = null,
    val approvedAt: LocalDateTime? = null,
) {
    companion object {
        fun of(approval: RoomApprovalEntity): RoomApprovalResponse {
            return RoomApprovalResponse(
                room = RoomResponse.of(approval.room),
                checkpointId = approval.checkpoint.id!!,
                checkpointName = approval.checkpoint.name,
                approved = true,
                approvedTeacher = approval.teacher?.let { UserResponse.Companion.of(it) },
                approvedAt = approval.updatedAt
            )
        }

        fun notApproved(room: RoomEntity, checkpoint: AttendanceCheckpointEntity): RoomApprovalResponse {
            return RoomApprovalResponse(
                room = RoomResponse.of(room),
                checkpointId = checkpoint.id!!,
                checkpointName = checkpoint.name,
                approved = false
            )
        }
    }
}