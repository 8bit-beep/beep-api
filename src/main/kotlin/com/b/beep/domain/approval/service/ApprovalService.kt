package com.b.beep.domain.approval.service

import com.b.beep.global.security.ContextHolder
import com.b.beep.domain.approval.controller.dto.request.ApproveRequest
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.domain.approval.entity.ApprovalEntity
import com.b.beep.domain.approval.error.ApprovalError
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.period.service.PeriodService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ApprovalService(
    private val approvalRepository: ApprovalRepository,
    private val contextHolder: ContextHolder,
    private val periodService: PeriodService,
) {
    @Transactional
    fun approve(request: ApproveRequest) {
        val period = periodService.getCurrentPeriod()
        val approval = approvalRepository.findByPeriodAndRoomAndDate(period, request.roomName, LocalDate.now())
            ?: throw CustomException(ApprovalError.APPROVAL_NOT_FOUND)

        if (approval.teacher == null) approval.teacher = contextHolder.user
        else approval.teacher = null

        approvalRepository.save(approval)
    }

    fun getNotApprovedRooms(): List<ApprovalEntity> {
        return approvalRepository
            .findAllByPeriodAndDateAndTeacherIsNull(periodService.getCurrentPeriod(), LocalDate.now())
    }

    fun getAllApprovalStatus(): List<ApprovalEntity> {
        return approvalRepository.findAllByPeriodAndDate(periodService.getCurrentPeriod(), LocalDate.now())
    }

    fun getApprovalStatusByRoom(room: Room): ApprovalEntity {
        val period = periodService.getCurrentPeriod()

        return approvalRepository.findByPeriodAndRoomAndDate(period, room, LocalDate.now())
            ?: throw CustomException(ApprovalError.APPROVAL_NOT_FOUND)
    }
}
