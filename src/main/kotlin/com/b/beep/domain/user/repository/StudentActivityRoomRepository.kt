package com.b.beep.domain.user.repository

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.DayOfWeek

interface StudentActivityRoomRepository : JpaRepository<StudentActivityRoomEntity, Long> {
    fun findAllByUser(user: UserEntity): List<StudentActivityRoomEntity>

    fun findByUserAndDayOfWeekAndType(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        type: AttendanceTypeEntity
    ): StudentActivityRoomEntity?

    @Query(
        """
        SELECT activityRoom
        FROM StudentActivityRoomEntity activityRoom
        WHERE activityRoom.user IN :users
          AND (activityRoom.dayOfWeek = :dayOfWeek OR activityRoom.dayOfWeek IS NULL)
        """
    )
    fun findAllByUserInAndDayOfWeekOrCommon(
        @Param("users") users: List<UserEntity>,
        @Param("dayOfWeek") dayOfWeek: DayOfWeek
    ): List<StudentActivityRoomEntity>

    fun deleteAllByUser(user: UserEntity)
}
