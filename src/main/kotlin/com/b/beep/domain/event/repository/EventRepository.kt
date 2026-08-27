package com.b.beep.domain.event.repository

import com.b.beep.domain.event.domain.entity.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findAllByDateOrderByIdAsc(date: LocalDate): List<EventEntity>
}
