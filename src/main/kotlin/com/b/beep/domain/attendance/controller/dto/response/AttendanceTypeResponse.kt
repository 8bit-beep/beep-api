package com.b.beep.domain.attendance.controller.dto.response

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity

data class AttendanceTypeResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(entity: AttendanceTypeEntity): AttendanceTypeResponse {
            return AttendanceTypeResponse(
                id = entity.id!!,
                name = entity.name
            )
        }
    }
}
