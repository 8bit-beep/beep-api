package com.b.beep.domain.user.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek

interface StudentScheduleRepository : JpaRepository<StudentScheduleEntity, Long> {
    fun findAllByUser(user: UserEntity): List<StudentScheduleEntity>
    fun findAllByUserAndDayOfWeek(user: UserEntity, dayOfWeek: DayOfWeek): List<StudentScheduleEntity>
    fun findByUserAndDayOfWeekAndCheckpoint(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity
    ): StudentScheduleEntity?

    fun findByUserAndDayOfWeekAndCheckpointAndType(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity,
        type: AttendanceTypeEntity
    ): StudentScheduleEntity?

    fun existsByUserAndDayOfWeekAndCheckpoint(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity
    ): Boolean

    fun existsByCheckpoint(checkpoint: AttendanceCheckpointEntity): Boolean
}
