package com.b.beep.domain.user.repository

import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentActivityRoomRepository : JpaRepository<StudentActivityRoomEntity, Long>
