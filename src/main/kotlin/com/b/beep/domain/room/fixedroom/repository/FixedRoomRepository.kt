package com.b.beep.domain.room.fixedroom.repository

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.room.entity.RoomEntity
import com.b.beep.domain.room.fixedroom.entity.FixedRoomEntity
import com.b.beep.domain.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FixedRoomRepository : JpaRepository<FixedRoomEntity, Long> {
    fun existsByUserAndTypeAndRoom(user: UserEntity, type: AttendanceType, room: RoomEntity): Boolean
    fun findAllByUser(user: UserEntity): List<FixedRoomEntity>
    fun findAllByUserAndType(user: UserEntity, type: AttendanceType): List<FixedRoomEntity>
}