package com.b.beep.domain.user.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek

interface StudentActivityRoomRepository : JpaRepository<StudentActivityRoomEntity, Long> {
    fun findAllByUser(user: UserEntity): List<StudentActivityRoomEntity>

    fun findByUserAndDayOfWeekAndType(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        type: AttendanceTypeEntity
    ): StudentActivityRoomEntity?

    fun deleteAllByUser(user: UserEntity)
}
