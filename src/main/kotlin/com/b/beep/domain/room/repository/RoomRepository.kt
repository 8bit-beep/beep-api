package com.b.beep.domain.room.repository

import com.b.beep.domain.room.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<RoomEntity, Long> {
}