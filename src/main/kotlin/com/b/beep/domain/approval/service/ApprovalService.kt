package com.b.beep.domain.approval.service

import com.b.beep.domain.approval.controller.dto.request.ApproveRequest
import com.b.beep.domain.approval.controller.dto.response.ApprovalResponse
import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.room.service.RoomService
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ApprovalService(
    private val approvalRepository: ApprovalRepository,
    private val roomRepository: RoomRepository,
    private val contextHolder: ContextHolder,
    private val periodResolver: PeriodResolver,
    private val roomService: RoomService
) {
    @Transactional
    fun approve(request: ApproveRequest) {
        val period = periodResolver.getCurrentPeriod()
        val room = roomService.getRoomById(request.roomId)
        val today = LocalDate.now()

        val approval = approvalRepository.findByPeriodAndRoomAndDate(period, room, today)

        if (approval == null) {
            approvalRepository.save(
                ApprovalEntity(
                    period = period,
                    room = room,
                    date = today,
                    teacher = contextHolder.user
                )
            )
        } else {
            approvalRepository.delete(approval)
        }
    }

    @Transactional(readOnly = true)
    fun getNotApprovedRooms(): List<ApprovalResponse> {
        val period = periodResolver.getCurrentPeriod()
        val today = LocalDate.now()
        val allRooms = roomRepository.findAll()
        val approvedRoomIds = approvalRepository
            .findAllByPeriodAndDate(period, today)
            .map { it.room.id }

        return allRooms
            .filter { it.id !in approvedRoomIds }
            .map { ApprovalResponse.notApproved(it, period) }
    }

    @Transactional(readOnly = true)
    fun getAllApprovalStatus(): List<ApprovalResponse> {
        val period = periodResolver.getCurrentPeriod()
        val today = LocalDate.now()
        val allRooms = roomRepository.findAll()
        val approvals = approvalRepository.findAllByPeriodAndDate(period, today)
        val approvalMap = approvals.associateBy { it.room.id }

        return allRooms.map { room ->
            approvalMap[room.id]?.let { ApprovalResponse.of(it) }
                ?: ApprovalResponse.notApproved(room, period)
        }
    }

    @Transactional(readOnly = true)
    fun getApprovalStatusByRoom(roomId: Long): ApprovalResponse {
        val period = periodResolver.getCurrentPeriod()
        val room = roomService.getRoomById(roomId)
        val approval = approvalRepository.findByPeriodAndRoomAndDate(period, room, LocalDate.now())

        return approval?.let { ApprovalResponse.of(it) }
            ?: ApprovalResponse.notApproved(room, period)
    }
}
