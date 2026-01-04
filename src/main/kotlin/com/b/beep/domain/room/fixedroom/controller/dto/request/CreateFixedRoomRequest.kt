package com.b.beep.domain.room.fixedroom.controller.dto.request

import com.b.beep.domain.attendance.domain.enums.AttendanceType

data class CreateFixedRoomRequest(
    val roomId: Long,
    val type: AttendanceType
)