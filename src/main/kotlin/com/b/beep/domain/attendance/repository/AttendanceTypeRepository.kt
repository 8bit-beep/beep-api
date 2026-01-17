package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AttendanceTypeRepository : JpaRepository<AttendanceTypeEntity, Long> {
    fun findByNameAndIsDeletedFalse(name: String): AttendanceTypeEntity?
    fun findAllByIsDeletedFalse(): List<AttendanceTypeEntity>
    fun findByIdAndIsDeletedFalse(id: Long): AttendanceTypeEntity?
    fun existsByNameAndIsDeletedFalse(name: String): Boolean
}
