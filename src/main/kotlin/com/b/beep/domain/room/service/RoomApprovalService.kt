package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.error.RoomApprovalError
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class RoomApprovalService(
    private val roomApprovalRepository: RoomApprovalRepository,
    private val roomRepository: RoomRepository,
    private val contextHolder: ContextHolder,
    private val checkpointResolver: CheckpointResolver,
    private val roomService: RoomService
) {
    fun createApproval(roomId: Long) {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val room = roomService.getRoomById(roomId)
        val today = LocalDate.now()

        if (roomApprovalRepository.existsByCheckpointAndRoomAndDate(checkpoint, room, today)) {
            throw CustomException(RoomApprovalError.ALREADY_APPROVED)
        }

        roomApprovalRepository.save(
            RoomApprovalEntity(
                checkpoint = checkpoint,
                room = room,
                date = today,
                teacher = contextHolder.user
            )
        )
    }

    @Transactional(readOnly = true)
    fun getApprovals(approved: Boolean?): List<RoomApprovalResponse> {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val today = LocalDate.now()
        val allRooms = roomRepository.findAll()
        val approvals = roomApprovalRepository.findAllByCheckpointAndDate(checkpoint, today)
        val approvalMap = approvals.associateBy { it.room.id }

        val responses = allRooms.map { room ->
            approvalMap[room.id]?.let { RoomApprovalResponse.Companion.of(it) }
                ?: RoomApprovalResponse.Companion.notApproved(room, checkpoint)
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
        val room = roomService.getRoomById(roomId)
        val approval = roomApprovalRepository.findByCheckpointAndRoomAndDate(checkpoint, room, LocalDate.now())

        return approval?.let { RoomApprovalResponse.Companion.of(it) }
            ?: RoomApprovalResponse.Companion.notApproved(room, checkpoint)
    }

    fun deleteApproval(roomId: Long) {
        val checkpoint = checkpointResolver.getCurrentCheckpoint()
        val room = roomService.getRoomById(roomId)
        val today = LocalDate.now()

        val approval = roomApprovalRepository.findByCheckpointAndRoomAndDate(checkpoint, room, today)
            ?: throw CustomException(RoomApprovalError.APPROVAL_NOT_FOUND)

        roomApprovalRepository.delete(approval)
    }
}
