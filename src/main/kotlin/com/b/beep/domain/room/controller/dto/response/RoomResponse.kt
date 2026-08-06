package com.b.beep.domain.room.controller.dto.response

import com.b.beep.domain.room.domain.entity.RoomEntity

data class RoomResponse(
    val id: Long,
    val name: String,
    val grade: Int?,
    val classNumber: Int?,
    val floor: Int?,
    val currentStudentCount: Int? = null
) {
    companion object {
        fun of(entity: RoomEntity, currentStudentCount: Int? = null): RoomResponse {
            return RoomResponse(
                id = entity.id!!,
                name = entity.name,
                grade = entity.grade,
                classNumber = entity.classNumber,
                floor = entity.floor,
                currentStudentCount = currentStudentCount
            )
        }
    }
}
