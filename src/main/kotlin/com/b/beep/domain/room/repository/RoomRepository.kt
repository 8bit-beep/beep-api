package com.b.beep.domain.room.repository

import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RoomRepository : JpaRepository<RoomEntity, Long> {
    fun existsByName(name: String): Boolean
    fun findByName(name: String): RoomEntity?
    fun findAllByIsDeletedFalseOrderByFloorAscNameAsc(): List<RoomEntity>
    fun findAllByIsDeletedFalse(): List<RoomEntity>
    fun findByIdAndIsDeletedFalse(id: Long): RoomEntity?
    fun existsByNameAndIsDeletedFalse(name: String): Boolean

    @Query("SELECT r FROM RoomEntity r WHERE r.isDeleted = false ORDER BY r.floor ASC NULLS LAST, r.grade ASC NULLS LAST, r.classNumber ASC NULLS LAST, r.name ASC")
    fun findAllSortedByFloorAndClass(): List<RoomEntity>
}
