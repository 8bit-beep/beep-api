package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.request.RoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RoomService(
    private val roomRepository: RoomRepository
) {
    fun findAll(): List<RoomResponse> {
        return roomRepository.findAll().map { RoomResponse.of(it) }
    }

    fun findById(id: Long): RoomResponse {
        val room = getRoomById(id)
        return RoomResponse.of(room)
    }

    @Transactional
    fun create(request: RoomRequest): RoomResponse {
        if (roomRepository.existsByName(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        val room = roomRepository.save(RoomEntity(name = request.name))
        return RoomResponse.of(room)
    }

    @Transactional
    fun update(id: Long, request: RoomRequest): RoomResponse {
        val room = getRoomById(id)
        if (room.name != request.name && roomRepository.existsByName(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        room.name = request.name
        return RoomResponse.of(room)
    }

    @Transactional
    fun delete(id: Long) {
        val room = getRoomById(id)
        roomRepository.delete(room)
    }

    fun getRoomById(id: Long): RoomEntity {
        return roomRepository.findById(id)
            .orElseThrow { CustomException(RoomError.ROOM_NOT_FOUND) }
    }
}
