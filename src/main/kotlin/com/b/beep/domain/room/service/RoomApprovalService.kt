package com.b.beep.domain.room.service

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import com.b.beep.domain.room.controller.dto.response.RoomApprovedTeacherResponse
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomApprovalError
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class RoomApprovalService(
    private val roomApprovalRepository: RoomApprovalRepository,
    private val roomRepository: RoomRepository,
    private val contextHolder: ContextHolder,
    private val checkpointResolver: CheckpointResolver
) {
    fun createApproval(roomId: Long) {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val room = getRoomEntity(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val today = LocalDate.now()

        if (roomApprovalRepository.existsByCheckpointAndRoomAndDate(checkpoint, room, today)) {
            throw CustomException(RoomApprovalError.ALREADY_APPROVED)
        }

        try {
            roomApprovalRepository.save(
                RoomApprovalEntity(
                    checkpoint = checkpoint,
                    room = room,
                    date = today,
                    teacher = contextHolder.user
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(RoomApprovalError.ALREADY_APPROVED)
        }
    }

    @Transactional(readOnly = true)
    fun getApprovals(approved: Boolean?): List<RoomApprovalResponse> {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val today = LocalDate.now()
        val approvalMap = roomApprovalRepository
            .findAllByCheckpointAndDate(checkpoint, today)
            .associateBy { it.room.id }

        val responses = roomRepository.findAll().map { room ->
            room.toResponse(approvalMap[room.id])
        }

        return when (approved) {
            true -> responses.filter { it.approved }
            false -> responses.filter { !it.approved }
            null -> responses
        }
    }

    @Transactional(readOnly = true)
    fun getApproval(roomId: Long): RoomApprovalResponse {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val room = getRoomEntity(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val approval = roomApprovalRepository.findByCheckpointAndRoomAndDate(checkpoint, room, LocalDate.now())
        return room.toResponse(approval)
    }

    fun deleteApproval(roomId: Long) {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val room = getRoomEntity(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val today = LocalDate.now()

        val approval = roomApprovalRepository.findByCheckpointAndRoomAndDate(checkpoint, room, today)
            ?: throw CustomException(RoomApprovalError.APPROVAL_NOT_FOUND)

        roomApprovalRepository.delete(approval)
    }

    private fun getRoomEntity(roomId: Long): RoomEntity? {
        return roomRepository.findByIdOrNull(roomId)
    }

    private fun RoomEntity.toResponse(approval: RoomApprovalEntity?): RoomApprovalResponse {
        return RoomApprovalResponse(
            room = RoomResponse.of(this),
            approved = approval != null,
            approvedTeacher = approval?.teacher?.let {
                RoomApprovedTeacherResponse(
                    id = it.id!!,
                    username = it.username,
                )
            },
            approvedAt = approval?.updatedAt
        )
    }
}
