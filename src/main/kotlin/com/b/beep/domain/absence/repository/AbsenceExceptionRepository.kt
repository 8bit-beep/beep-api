package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceExceptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AbsenceExceptionRepository : JpaRepository<AbsenceExceptionEntity, Long> {
    fun findAllByAbsenceId(absenceId: Long): List<AbsenceExceptionEntity>
    fun deleteAllByAbsenceId(absenceId: Long)
}
