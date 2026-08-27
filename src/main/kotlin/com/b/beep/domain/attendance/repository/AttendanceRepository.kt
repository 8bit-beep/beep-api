package com.b.beep.domain.attendance.repository

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AttendanceRepository : JpaRepository<AttendanceEntity, Long> {
    fun findByCheckpointAndUserAndDate(
        checkpoint: AttendanceCheckpointEntity,
        user: UserEntity,
        date: LocalDate
    ): AttendanceEntity?

    fun findAllByUser(user: UserEntity): List<AttendanceEntity>
    fun findAllByUserAndDate(user: UserEntity, date: LocalDate): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user.id = :userId AND a.checkpoint.id = :checkpointId AND a.date = :date")
    fun findByUserIdAndCheckpointIdAndDate(
        @Param("userId") userId: Long,
        @Param("checkpointId") checkpointId: Long,
        @Param("date") date: LocalDate
    ): AttendanceEntity?

    fun findAllByCheckpointAndDateAndType(
        checkpoint: AttendanceCheckpointEntity,
        date: LocalDate,
        type: AttendanceTypeEntity
    ): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user IN :users AND a.checkpoint.id = :checkpointId AND a.date = :date")
    fun findAllByUsersAndCheckpointIdAndDate(
        @Param("users") users: List<UserEntity>,
        @Param("checkpointId") checkpointId: Long,
        @Param("date") date: LocalDate
    ): List<AttendanceEntity>

    fun existsByCheckpoint(checkpoint: AttendanceCheckpointEntity): Boolean

    fun deleteAllByAbsenceAndDateGreaterThanEqual(absence: AbsenceEntity, date: LocalDate)

    fun deleteAllByAbsence(absence: AbsenceEntity)

    fun deleteAllByEvent(event: EventEntity)

    fun findAllByDate(date: LocalDate): List<AttendanceEntity>

    @Query("SELECT DISTINCT a.room FROM AttendanceEntity a WHERE a.checkpoint = :checkpoint AND a.date = :date AND a.room IS NOT NULL")
    fun findDistinctRoomsByCheckpointAndDate(
        @Param("checkpoint") checkpoint: AttendanceCheckpointEntity,
        @Param("date") date: LocalDate
    ): List<RoomEntity>
}