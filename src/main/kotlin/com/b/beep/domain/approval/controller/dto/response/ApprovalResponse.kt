package com.b.beep.domain.approval.controller.dto.response

import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.user.controller.dto.response.UserResponse
import java.time.LocalDateTime

data class ApprovalResponse(
    val room: Room,
    val period: Int,
    val approvedTeacher: UserResponse? = null,
    val approvedAt: LocalDateTime,
) {
    companion object {
        fun of(approval: ApprovalEntity): ApprovalResponse {
            return ApprovalResponse(
                room = approval.room,
                period = approval.period,
                approvedTeacher = approval.teacher?.let { UserResponse.of(it) },
                approvedAt = approval.updatedAt!!
            )
        }
    }
}
