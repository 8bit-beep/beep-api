package com.b.beep.domain.event.repository

import com.b.beep.domain.event.domain.entity.EventUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EventUserRepository : JpaRepository<EventUserEntity, Long> {
    fun findAllByEventId(eventId: Long): List<EventUserEntity>
    fun findAllByEventIdIn(eventIds: List<Long>): List<EventUserEntity>
    fun deleteAllByEventId(eventId: Long)
}
