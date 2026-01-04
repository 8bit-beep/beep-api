package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
) {
    fun createRoom(request: CreateRoomRequest) {
        roomRepository.save(
            RoomEntity(
                name = request.name,
                grade = request.grade,
                classNumber = request.classNumber,
            )
        )
    }

    fun getRooms(): List<RoomResponse> {
        return roomRepository.findAll().map { RoomResponse.from(it) }
    }

    fun updateRoom(roomId: Long, request: UpdateRoomRequest) {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)

        room.name = request.name ?: room.name
        room.grade = request.grade ?: room.grade
        room.classNumber = request.classNumber ?: room.classNumber
    }

    fun deleteRoom(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        roomRepository.delete(room)
    }
}