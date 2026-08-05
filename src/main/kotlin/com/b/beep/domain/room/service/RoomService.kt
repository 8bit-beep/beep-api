package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.RoomClubNameResolver
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class RoomService(
    private val roomRepository: RoomRepository,
    private val roomClubNameResolver: RoomClubNameResolver
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
        val rooms = roomRepository.findAllByIsDeletedFalse()
            .sortedWith(compareBy(
                { it.floor ?: Int.MAX_VALUE },
                { getRoomSortPriority(it) },
                { it.grade ?: Int.MAX_VALUE },
                { it.classNumber ?: Int.MAX_VALUE },
                { it.name }
            ))
        val displayNameByRoomId = roomClubNameResolver.resolveDisplayNames(rooms, getToday())
        return rooms.map { RoomResponse.of(it, displayNameByRoomId[it.id] ?: it.name) }
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
        val displayName = roomClubNameResolver.resolveDisplayNames(listOf(room), getToday())[room.id] ?: room.name
        return RoomResponse.of(room, displayName)
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

    private fun getToday(): LocalDate {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
    }
}
