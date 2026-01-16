package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AttendanceRepository : JpaRepository<AttendanceEntity, Long> {
    fun findByCheckpointAndUserAndDate(checkpoint: AttendanceCheckpointEntity, user: UserEntity, date: LocalDate): AttendanceEntity?
    fun findAllByUser(user: UserEntity): List<AttendanceEntity>
    fun findAllByUserAndDate(user: UserEntity, date: LocalDate): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user.id = :userId AND a.checkpoint.id = :checkpointId AND a.date = :date")
    fun findByUserIdAndCheckpointIdAndDate(
        @Param("userId") userId: Long,
        @Param("checkpointId") checkpointId: Long,
        @Param("date") date: LocalDate
    ): AttendanceEntity?

    fun findAllByCheckpointAndDateAndType(checkpoint: AttendanceCheckpointEntity, date: LocalDate, type: AttendanceType): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user IN :users AND a.checkpoint.id = :checkpointId AND a.date = :date")
    fun findAllByUsersAndCheckpointIdAndDate(
        @Param("users") users: List<UserEntity>,
        @Param("checkpointId") checkpointId: Long,
        @Param("date") date: LocalDate
    ): List<AttendanceEntity>
}