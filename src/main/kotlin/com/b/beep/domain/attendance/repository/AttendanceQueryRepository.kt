package com.b.beep.domain.attendance.repository

import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AttendanceQueryRepository(
    private val attendanceRepository: AttendanceRepository,
    private val periodResolver: PeriodResolver,
    private val absenceRepository: AbsenceRepository
) {
    fun findCurrentStatus(user: UserEntity): AttendanceType {
        val today = LocalDate.now()
        val currentPeriod = periodResolver.getCurrentPeriod()
        if (currentPeriod == 0) return AttendanceType.NOT_ATTEND

        val attendance = attendanceRepository.findByPeriodAndUserAndDate(currentPeriod, user, today)
        if (attendance != null) return attendance.type

        val isAbsent = absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            user.id!!, today, today
        )
        return if (isAbsent) AttendanceType.OUTGOING else AttendanceType.NOT_ATTEND
    }

    fun findAllCurrentStatuses(users: List<UserEntity>): Map<Long, AttendanceType> {
        if (users.isEmpty()) return emptyMap()
        val today = LocalDate.now()
        val currentPeriod = periodResolver.getCurrentPeriod()
        if (currentPeriod == 0) return users.associate { it.id!! to AttendanceType.NOT_ATTEND }

        val attendances = attendanceRepository.findAllByUsersAndPeriodAndDate(users, currentPeriod, today)
        val attendanceMap = attendances.associate { it.user.id!! to it.type }

        val absences = absenceRepository.findAllByStartDateLessThanEqualAndEndDateGreaterThanEqualWithUser(today, today)
        val absentUserIds = absences.map { it.user.id }.toSet()

        return users.associate { user ->
            val status = attendanceMap[user.id]
                ?: if (absentUserIds.contains(user.id)) AttendanceType.OUTGOING else AttendanceType.NOT_ATTEND
            user.id!! to status
        }
    }
}
