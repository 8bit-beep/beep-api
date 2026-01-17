package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RoomService(
    private val roomRepository: RoomRepository
) {
    fun createRoom(request: CreateRoomRequest): RoomResponse {
        if (roomRepository.existsByName(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        val room = roomRepository.save(
            RoomEntity(
                name = request.name,
                grade = request.grade,
                classNumber = request.classNumber,
                floor = request.floor
            )
        )
        return RoomResponse.of(room)
    }

    @Transactional(readOnly = true)
    fun getRooms(): List<RoomResponse> {
        return roomRepository.findAll().map { RoomResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getRoom(roomId: Long): RoomResponse {
        val room = getRoomById(roomId)
        return RoomResponse.of(room)
    }

    fun updateRoom(roomId: Long, request: UpdateRoomRequest): RoomResponse {
        val room = getRoomById(roomId)
        if (room.name != request.name && roomRepository.existsByName(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        room.name = request.name
        room.grade = request.grade
        room.classNumber = request.classNumber
        room.floor = request.floor
        return RoomResponse.of(room)
    }

    fun deleteRoom(roomId: Long) {
        val room = getRoomById(roomId)
        roomRepository.delete(room)
    }

    fun getRoomById(roomId: Long): RoomEntity {
        return roomRepository.findById(roomId)
            .orElseThrow { CustomException(RoomError.ROOM_NOT_FOUND) }
    }
}
