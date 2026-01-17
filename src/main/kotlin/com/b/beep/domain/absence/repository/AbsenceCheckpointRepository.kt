package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceCheckpointEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AbsenceCheckpointRepository : JpaRepository<AbsenceCheckpointEntity, Long> {
    fun findAllByAbsenceId(absenceId: Long): List<AbsenceCheckpointEntity>

    fun deleteAllByAbsenceId(absenceId: Long)
}
