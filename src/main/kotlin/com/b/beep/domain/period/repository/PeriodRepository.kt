package com.b.beep.domain.period.repository

import com.b.beep.domain.period.domain.entity.PeriodEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PeriodRepository : JpaRepository<PeriodEntity, Long> {
    fun findByPeriod(period: Int): PeriodEntity?
    fun existsByPeriod(period: Int): Boolean
    fun deleteByPeriod(period: Int)
}
