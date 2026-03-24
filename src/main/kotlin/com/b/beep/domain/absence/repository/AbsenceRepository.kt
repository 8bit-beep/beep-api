package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface AbsenceRepository : JpaRepository<AbsenceEntity, Long> {
    fun findAllByEndDateBefore(date: LocalDate): List<AbsenceEntity>

    fun findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        rangeEnd: LocalDate,
        rangeStart: LocalDate
    ): List<AbsenceEntity>
    fun findAllByIsDeletedFalseOrderByStartDateAscEndDateAsc(pageable: Pageable): Page<AbsenceEntity>
    fun findByIdAndIsDeletedFalse(id: Long): AbsenceEntity?
    @Query("""
    SELECT a FROM AbsenceEntity a
    WHERE a.isDeleted = false
      AND a.startDate <= :today
      AND a.endDate >= :today
    ORDER BY a.startDate ASC
""")
    fun findAllByDateAndIsDeletedFalse(
        @Param("today") today: LocalDate,
        pageable: Pageable
    ): Page<AbsenceEntity>
}