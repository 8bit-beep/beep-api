package com.b.beep.domain.shift.controller.dto.response

import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.shift.domain.entity.ShiftEntity
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import java.time.LocalDate

data class ShiftResponse(
    val id: Long,
    val user: UserResponse,
    val room: RoomResponse,
    val period: Int,
    val reason: String,
    val status: ShiftStatus,
    val date: LocalDate,
) {
    companion object {
        fun of(shift: ShiftEntity, studentInfo: StudentInfoEntity? = null): ShiftResponse {
            return ShiftResponse(
                id = shift.id!!,
                user = UserResponse.of(shift.user, studentInfo),
                room = RoomResponse.of(shift.room),
                period = shift.period,
                reason = shift.reason,
                status = shift.status,
                date = shift.date,
            )
        }
    }
}