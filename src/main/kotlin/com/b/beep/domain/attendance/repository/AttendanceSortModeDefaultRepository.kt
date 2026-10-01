package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeDefaultEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek

interface AttendanceSortModeDefaultRepository : JpaRepository<AttendanceSortModeDefaultEntity, Long> {
    fun findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(
        dayOfWeek: DayOfWeek,
        checkpoints: Collection<AttendanceCheckpointEntity>
    ): List<AttendanceSortModeDefaultEntity>
}
