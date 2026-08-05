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
        if (roomRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        val room = roomRepository.save(
            RoomEntity(
                name = request.name,
                grade = request.grade,
                classNumber = request.classNumber,
                floor = request.floor,
                clubName = request.clubName
            )
        )
        return RoomResponse.of(room)
    }

    @Transactional(readOnly = true)
    fun getRooms(): List<RoomResponse> {
        return roomRepository.findAllByIsDeletedFalse()
            .sortedWith(compareBy(
                { it.floor ?: Int.MAX_VALUE },
                { getRoomSortPriority(it) },
                { it.grade ?: Int.MAX_VALUE },
                { it.classNumber ?: Int.MAX_VALUE },
                { it.name }
            ))
            .map { RoomResponse.of(it) }
    }

    private fun getRoomSortPriority(room: RoomEntity): Int {
        return when {
            room.grade != null && room.classNumber != null -> 0
            room.name.startsWith("프로젝트") -> 1
            room.name.contains("랩") -> 2
            else -> 3
        }
    }

    @Transactional(readOnly = true)
    fun getRoom(roomId: Long): RoomResponse {
        val room = getRoomEntityById(roomId)
        return RoomResponse.of(room)
    }

    fun updateRoom(roomId: Long, request: UpdateRoomRequest): RoomResponse {
        val room = getRoomEntityById(roomId)
        if (room.name != request.name && roomRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        room.name = request.name
        room.grade = request.grade
        room.classNumber = request.classNumber
        room.floor = request.floor
        room.clubName = request.clubName
        return RoomResponse.of(room)
    }

    fun deleteRoom(roomId: Long) {
        val room = getRoomEntityById(roomId)
        room.name = "${room.name}_deleted_${room.id}"
        room.isDeleted = true
    }

    fun getRoomEntityById(roomId: Long): RoomEntity {
        return roomRepository.findByIdAndIsDeletedFalse(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
    }
}
