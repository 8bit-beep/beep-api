package com.b.beep.domain.room.repository

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RoomApprovalRepository : JpaRepository<RoomApprovalEntity, Long> {
    fun findByCheckpointAndRoomAndDate(
        checkpoint: AttendanceCheckpointEntity,
        room: RoomEntity,
        date: LocalDate
    ): RoomApprovalEntity?

    fun findAllByCheckpointAndDate(
        checkpoint: AttendanceCheckpointEntity,
        date: LocalDate
    ): List<RoomApprovalEntity>

    fun findAllByDate(date: LocalDate): List<RoomApprovalEntity>

    fun existsByCheckpointAndRoomAndDate(
        checkpoint: AttendanceCheckpointEntity,
        room: RoomEntity,
        date: LocalDate
    ): Boolean

    @Query("SELECT ra.room.id FROM RoomApprovalEntity ra WHERE ra.checkpoint = :checkpoint AND ra.date = :date")
    fun findApprovedRoomIdsByCheckpointAndDate(
        @Param("checkpoint") checkpoint: AttendanceCheckpointEntity,
        @Param("date") date: LocalDate
    ): List<Long>
}
