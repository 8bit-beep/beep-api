package com.b.beep.domain.user.service

import com.b.beep.global.security.ContextHolder
import com.b.beep.domain.user.controller.dto.request.AddFixedRoomRequest
import com.b.beep.domain.user.controller.dto.request.UpdateFixedRoomRequest
import com.b.beep.domain.user.entity.FixedRoomEntity
import com.b.beep.domain.user.repository.FixedRoomRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.domain.user.domain.FixedRoomError
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FixedRoomService(
    private val fixedRoomRepository: FixedRoomRepository,
    private val contextHolder: ContextHolder
) {
    fun add(request: AddFixedRoomRequest) {
        val user = contextHolder.user

        if (fixedRoomRepository.existsByUserAndTypeAndRoom(user, request.type, request.room)) {
            throw CustomException(FixedRoomError.ALREADY_EXIST_COMBINATION)
        }

        val fixedRoom = FixedRoomEntity(
            user = user,
            room = request.room,
            type = request.type,
        )
        fixedRoomRepository.save(fixedRoom)
    }

    fun update(fixedRoomId: Long, request: UpdateFixedRoomRequest) {
        val fixedRoom = fixedRoomRepository.findByIdOrNull(fixedRoomId)
            ?: throw CustomException(FixedRoomError.FIXED_ROOM_NOT_FOUND)
        val user = contextHolder.user

        if (fixedRoom.user.id != user.id) throw CustomException(FixedRoomError.NO_PERMISSION_TO_UPDATE)

        val finalType = request.type ?: fixedRoom.type
        val finalRoom = request.room ?: fixedRoom.room

        val existingFixedRooms = fixedRoomRepository.findAllByUser(user)
        val conflict = existingFixedRooms.any {
            it.id != fixedRoomId && it.type == finalType && it.room == finalRoom
        }
        if (conflict) {
            throw CustomException(FixedRoomError.ALREADY_EXIST_COMBINATION)
        }

        request.room?.let { newRoom ->
            fixedRoom.room = newRoom
        }

        request.type?.let { newType ->
            fixedRoom.type = newType
        }

        fixedRoomRepository.save(fixedRoom)
    }

    fun delete(fixedRoomId: Long) {
        val fixedRoom = fixedRoomRepository.findByIdOrNull(fixedRoomId)
            ?: throw CustomException(FixedRoomError.FIXED_ROOM_NOT_FOUND)
        val user = contextHolder.user

        if (fixedRoom.user.id != user.id) throw CustomException(FixedRoomError.NO_PERMISSION_TO_UPDATE)

        fixedRoomRepository.delete(fixedRoom)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<FixedRoomEntity> {
        val user = contextHolder.user
        return fixedRoomRepository.findAllByUser(user)
    }
}
