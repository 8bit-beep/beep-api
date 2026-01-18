package com.b.beep.domain.shift.repository

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.shift.domain.entity.ShiftEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ShiftRepository : JpaRepository<ShiftEntity, Long> {
    fun findAllByUserAndDate(user: UserEntity, date: LocalDate): List<ShiftEntity>
    fun existsByUserAndDateAndCheckpoint(
        user: UserEntity,
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity
    ): Boolean

    fun existsByUserAndDateAndCheckpointAndIdNot(
        user: UserEntity,
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity,
        id: Long
    ): Boolean

    fun findAllByDateGreaterThanEqual(date: LocalDate): List<ShiftEntity>
    fun deleteByDateBefore(date: LocalDate)
    fun existsByCheckpoint(checkpoint: AttendanceCheckpointEntity): Boolean
}
