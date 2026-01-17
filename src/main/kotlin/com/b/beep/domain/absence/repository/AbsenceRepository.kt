package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AbsenceRepository : JpaRepository<AbsenceEntity, Long> {
    fun findAllByEndDateBefore(date: LocalDate): List<AbsenceEntity>

    fun findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        rangeEnd: LocalDate,
        rangeStart: LocalDate
    ): List<AbsenceEntity>
}