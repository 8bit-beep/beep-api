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

        request.room?.let { newRoom ->
            fixedRoom.room = newRoom
        }

        // type 중복 검사
        request.type?.let { newType ->
            val conflict = fixedRoomRepository.findAllByUser(user)
                .any { it.type == newType && it.id != fixedRoomId }
            if (conflict) throw CustomException(FixedRoomError.ALREADY_EXIST_TYPE)
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
