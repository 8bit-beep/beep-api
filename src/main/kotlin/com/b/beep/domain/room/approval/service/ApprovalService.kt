package com.b.beep.domain.room.approval.service

import com.b.beep.global.security.ContextHolder
import com.b.beep.domain.room.approval.controller.dto.request.ApproveRequest
import com.b.beep.domain.room.approval.repository.ApprovalRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.domain.room.approval.entity.ApprovalEntity
import com.b.beep.domain.room.approval.error.ApprovalError
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.room.approval.controller.dto.response.ApprovalResponse
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ApprovalService(
    private val approvalRepository: ApprovalRepository,
    private val contextHolder: ContextHolder,
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun approve(request: ApproveRequest) {
        val period = PeriodResolver.getCurrentPeriod()
        val room = roomRepository.findByIdOrNull(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val approval = approvalRepository.findByPeriodAndRoomAndDate(period, room, LocalDate.now())
            ?: throw CustomException(ApprovalError.APPROVAL_NOT_FOUND)

        if (approval.teacher == null) approval.teacher = contextHolder.user
        else approval.teacher = null

        approvalRepository.save(approval)
    }

    fun getNotApprovedRooms(): List<ApprovalResponse> {
        return approvalRepository
            .findAllByPeriodAndDateAndTeacherIsNull(PeriodResolver.getCurrentPeriod(), LocalDate.now())
            .map { ApprovalResponse.from(it) }
    }

    fun getAllApprovalStatus(): List<ApprovalResponse> {
        return approvalRepository.findAllByPeriodAndDate(PeriodResolver.getCurrentPeriod(), LocalDate.now())
            .map { ApprovalResponse.from(it) }
    }

    fun getApprovalStatusByRoom(roomId: Long): ApprovalResponse {
        val period = PeriodResolver.getCurrentPeriod()
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw CustomException(ApprovalError.APPROVAL_NOT_FOUND)

        val approval = approvalRepository.findByPeriodAndRoomAndDate(period, room, LocalDate.now())
            ?: throw CustomException(ApprovalError.APPROVAL_NOT_FOUND)

        return ApprovalResponse.from(approval)
    }
}
