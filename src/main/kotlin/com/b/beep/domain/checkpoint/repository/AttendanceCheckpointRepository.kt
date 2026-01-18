package com.b.beep.domain.checkpoint.repository

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AttendanceCheckpointRepository : JpaRepository<AttendanceCheckpointEntity, Long> {
    fun findAllByIsDeletedFalse(): List<AttendanceCheckpointEntity>
    fun findByIdAndIsDeletedFalse(id: Long): AttendanceCheckpointEntity?
}
