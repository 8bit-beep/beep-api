package com.b.beep.domain.approval.repository

import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApprovalRepository : JpaRepository<ApprovalEntity, Long> {
    fun findByPeriodAndRoomAndDate(period: Int, room: RoomEntity, date: LocalDate): ApprovalEntity?
    fun findAllByPeriodAndDate(period: Int, date: LocalDate): List<ApprovalEntity>
}
