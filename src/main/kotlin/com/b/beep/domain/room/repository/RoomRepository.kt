package com.b.beep.domain.room.repository

import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<RoomEntity, Long> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): RoomEntity?
}
