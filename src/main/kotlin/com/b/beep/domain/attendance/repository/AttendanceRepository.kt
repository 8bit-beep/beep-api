package com.b.beep.domain.attendance.repository

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface AttendanceRepository : JpaRepository<AttendanceEntity, Long> {
    fun findByPeriodAndUserAndDate(period: Int, user: UserEntity, date: LocalDate): AttendanceEntity?
    fun findAllByUser(user: UserEntity): List<AttendanceEntity>
    fun findAllByUserAndDate(user: UserEntity, date: LocalDate): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user.id = :userId AND a.period = :period AND a.date = :date")
    fun findByUserIdAndPeriodAndDate(
        @Param("userId") userId: Long,
        @Param("period") period: Int,
        @Param("date") date: LocalDate
    ): AttendanceEntity?

    fun findAllByPeriodAndDateAndType(period: Int, date: LocalDate, type: AttendanceType): List<AttendanceEntity>

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user IN :users AND a.period = :period AND a.date = :date")
    fun findAllByUsersAndPeriodAndDate(
        @Param("users") users: List<UserEntity>,
        @Param("period") period: Int,
        @Param("date") date: LocalDate
    ): List<AttendanceEntity>
}