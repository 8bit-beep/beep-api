package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AbsenceUserRepository : JpaRepository<AbsenceUserEntity, Long> {
    fun findAllByAbsenceId(absenceId: Long): List<AbsenceUserEntity>

    fun existsByUserIdAndAbsenceStartDateLessThanEqualAndAbsenceEndDateGreaterThanEqual(
        userId: Long,
        endDate: LocalDate,
        startDate: LocalDate
    ): Boolean

    fun existsByUserIdAndAbsenceStartDateLessThanEqualAndAbsenceEndDateGreaterThanEqualAndAbsenceIdNot(
        userId: Long,
        endDate: LocalDate,
        startDate: LocalDate,
        absenceId: Long
    ): Boolean

    fun deleteAllByAbsenceId(absenceId: Long)
}
