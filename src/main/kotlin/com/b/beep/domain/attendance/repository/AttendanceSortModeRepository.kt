package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AttendanceSortModeRepository : JpaRepository<AttendanceSortModeEntity, Long> {
    fun findAllByDateAndCheckpoint(
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity
    ): List<AttendanceSortModeEntity>

    fun findAllByDateAndCheckpointIn(
        date: LocalDate,
        checkpoints: Collection<AttendanceCheckpointEntity>
    ): List<AttendanceSortModeEntity>

    fun findByDateAndCheckpointAndGrade(
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity,
        grade: Int
    ): AttendanceSortModeEntity?

    fun deleteByDateAndCheckpointAndGrade(
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity,
        grade: Int
    )
}
