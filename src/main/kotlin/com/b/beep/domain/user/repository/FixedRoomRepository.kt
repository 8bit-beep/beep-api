package com.b.beep.domain.user.repository

import com.b.beep.domain.user.entity.FixedRoomEntity
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import org.springframework.data.jpa.repository.JpaRepository

interface FixedRoomRepository : JpaRepository<FixedRoomEntity, Long> {
    fun existsByUserAndRoom(user: UserEntity, room: Room): Boolean
    fun existsByUserAndType(user: UserEntity, type: AttendanceType): Boolean
    fun existsByUserAndTypeAndRoom(user: UserEntity, type: AttendanceType, room: Room): Boolean
    fun findByUserAndType(user: UserEntity, type: AttendanceType): FixedRoomEntity?
    fun findAllByUser(user: UserEntity): List<FixedRoomEntity>
    fun findAllByUserAndType(user: UserEntity, type: AttendanceType): List<FixedRoomEntity>
}
