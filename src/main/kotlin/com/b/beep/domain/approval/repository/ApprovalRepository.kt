package com.b.beep.domain.approval.repository

import com.b.beep.domain.approval.domain.entity.ApprovalEntity
import com.b.beep.domain.attendance.domain.enums.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ApprovalRepository : JpaRepository<ApprovalEntity, Long> {
    fun findByPeriodAndRoomAndDate(period: Int, room: Room, date: LocalDate): ApprovalEntity?
    fun findAllByPeriodAndDateAndTeacherIsNull(period: Int, date: LocalDate): List<ApprovalEntity>
    fun findAllByPeriodAndDate(period: Int, date: LocalDate): List<ApprovalEntity>
    fun findAllByPeriodAndTeacherIsNotNull(period: Int): List<ApprovalEntity>

    @Modifying
    @Query("UPDATE ApprovalEntity a SET a.teacher = null")
    fun resetAllApprovals()
}
