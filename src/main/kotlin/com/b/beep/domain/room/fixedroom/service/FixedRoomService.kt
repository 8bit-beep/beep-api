package com.b.beep.domain.room.fixedroom.service

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.fixedroom.controller.dto.request.CreateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.entity.FixedRoomEntity
import com.b.beep.domain.room.fixedroom.repository.FixedRoomRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.room.fixedroom.controller.dto.request.UpdateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.controller.dto.response.FixedRoomResponse
import com.b.beep.domain.room.fixedroom.error.FixedRoomError
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FixedRoomService(
    private val fixedRoomRepository: FixedRoomRepository,
    private val contextHolder: ContextHolder,
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun createFixedRoom(request: CreateFixedRoomRequest) {
        val user = contextHolder.user
        val room = roomRepository.findByIdOrNull(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)

        if (fixedRoomRepository.existsByUserAndTypeAndRoom(user, request.type, room)) {
            throw CustomException(FixedRoomError.ALREADY_EXIST_COMBINATION)
        }

        fixedRoomRepository.save(
            FixedRoomEntity(
                user = user,
                room = room,
                type = request.type
            )
        )
    }

    @Transactional(readOnly = true)
    fun getAll(): List<FixedRoomResponse> {
        val user = contextHolder.user
        return fixedRoomRepository.findAllByUser(user).map { FixedRoomResponse.from(it) }
    }

    @Transactional
    fun update(fixedRoomId: Long, request: UpdateFixedRoomRequest) {
        val user = contextHolder.user
        val fixedRoom = fixedRoomRepository.findByIdOrNull(fixedRoomId)
            ?: throw CustomException(FixedRoomError.FIXED_ROOM_NOT_FOUND)

        if (fixedRoom.user.id != user.id) throw CustomException(FixedRoomError.NO_PERMISSION_TO_UPDATE)

        val room = request.roomId?.let { roomRepository.findByIdOrNull(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND) }

        val finalType = request.type ?: fixedRoom.type
        val finalRoom = room ?: fixedRoom.room

        if (isConflict(user, fixedRoomId, finalType, finalRoom)) {
            throw CustomException(FixedRoomError.ALREADY_EXIST_COMBINATION)
        }

        fixedRoom.room = finalRoom
        fixedRoom.type = finalType

        fixedRoomRepository.save(fixedRoom)
    }

    @Transactional
    fun delete(fixedRoomId: Long) {
        val fixedRoom = fixedRoomRepository.findByIdOrNull(fixedRoomId)
            ?: throw CustomException(FixedRoomError.FIXED_ROOM_NOT_FOUND)
        val user = contextHolder.user

        if (fixedRoom.user.id != user.id) throw CustomException(FixedRoomError.NO_PERMISSION_TO_UPDATE)

        fixedRoomRepository.delete(fixedRoom)
    }

    private fun isConflict(
        user: UserEntity,
        fixedRoomId: Long,
        finalType: AttendanceType,
        finalRoom: RoomEntity
    ): Boolean {
        val existingFixedRooms = fixedRoomRepository.findAllByUser(user)

        return existingFixedRooms.any {
            it.id != fixedRoomId && it.type == finalType && it.room == finalRoom
        }
    }
}
