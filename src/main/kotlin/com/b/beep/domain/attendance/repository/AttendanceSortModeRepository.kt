package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AttendanceSortModeRepository : JpaRepository<AttendanceSortModeEntity, Long>
