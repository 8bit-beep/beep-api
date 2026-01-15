package com.b.beep.domain.absence.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AbsenceRepository : JpaRepository<AbsenceEntity, Long> {
    fun findAllByEndDateBefore(date: LocalDate): List<AbsenceEntity>

    @org.springframework.data.jpa.repository.Query(
        "SELECT a FROM AbsenceEntity a JOIN FETCH a.user WHERE a.startDate <= :endDate AND a.endDate >= :startDate"
    )
    fun findAllByStartDateLessThanEqualAndEndDateGreaterThanEqualWithUser(
        @org.springframework.data.repository.query.Param("startDate") startDate: LocalDate,
        @org.springframework.data.repository.query.Param("endDate") endDate: LocalDate
    ): List<AbsenceEntity>

    fun findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AbsenceEntity>

    /**
     * 해당 사용자의 날짜 범위와 겹치는 장기결석이 있는지 확인
     * (startDate <= endDate && endDate >= startDate) 조건으로 겹치는 날짜 범위를 검색
     */
    fun existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        userId: Long,
        endDate: LocalDate,
        startDate: LocalDate
    ): Boolean

    fun existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
        userId: Long,
        endDate: LocalDate,
        startDate: LocalDate,
        id: Long
    ): Boolean
}