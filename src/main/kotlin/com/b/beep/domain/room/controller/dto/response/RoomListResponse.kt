package com.b.beep.domain.room.controller.dto.response

data class RoomListResponse(
    val id: Long,
    val name: String,
    val grade: Int?,
    val classNumber: Int?,
    val floor: Int?,
    val clubName: String?,
    val currentStudentCount: Int?,
    val beacon: BeaconResponse?
) {
    companion object {
        fun of(room: RoomResponse, beacon: BeaconResponse?): RoomListResponse {
            return RoomListResponse(
                id = room.id,
                name = room.name,
                grade = room.grade,
                classNumber = room.classNumber,
                floor = room.floor,
                clubName = room.clubName,
                currentStudentCount = room.currentStudentCount,
                beacon = beacon
            )
        }
    }
}
