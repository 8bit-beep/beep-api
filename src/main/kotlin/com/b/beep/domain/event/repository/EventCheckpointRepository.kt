package com.b.beep.domain.event.repository

import com.b.beep.domain.event.domain.entity.EventCheckpointEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EventCheckpointRepository : JpaRepository<EventCheckpointEntity, Long> {
    fun findAllByEventId(eventId: Long): List<EventCheckpointEntity>
    fun findAllByEventIdIn(eventIds: List<Long>): List<EventCheckpointEntity>
    fun deleteAllByEventId(eventId: Long)
}
