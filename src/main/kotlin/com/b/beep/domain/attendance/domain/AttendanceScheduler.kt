package com.b.beep.domain.attendance.domain

import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.approval.repository.ApprovalRepository
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.room.repository.RoomRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class AttendanceScheduler(
    private val approvalRepository: ApprovalRepository,
    private val periodRepository: PeriodRepository,
    private val roomRepository: RoomRepository,
) {
    @Scheduled(cron = "0 10 0 * * MON-FRI")
    @Transactional
    fun createApprovalsToday() {
        val today = LocalDate.now()
        val periods = periodRepository.findAll().map { it.period }
        val rooms = roomRepository.findAll()

        val approvals = rooms.flatMap { room ->
            periods.map { period ->
                ApprovalEntity(
                    room = room,
                    period = period,
                    date = today
                )
            }
        }
        approvalRepository.saveAll(approvals)
    }
}