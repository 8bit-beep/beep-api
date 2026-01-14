package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.user.entity.UserEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AttendanceQueryService(
    private val attendanceRepository: AttendanceRepository,
    private val periodResolver: PeriodResolver
) {
    fun getCurrentStatus(user: UserEntity): AttendanceType {
        val currentPeriod = periodResolver.getCurrentPeriod()
        if (currentPeriod == 0) return AttendanceType.NOT_ATTEND
        return attendanceRepository
            .findByPeriodAndUserAndDate(currentPeriod, user, LocalDate.now())
            ?.type ?: AttendanceType.NOT_ATTEND
    }

    fun getCurrentStatuses(users: List<UserEntity>): Map<Long, AttendanceType> {
        if (users.isEmpty()) return emptyMap()
        val currentPeriod = periodResolver.getCurrentPeriod()
        if (currentPeriod == 0) return users.associate { it.id!! to AttendanceType.NOT_ATTEND }
        val attendances = attendanceRepository
            .findAllByUsersAndPeriodAndDate(users, currentPeriod, LocalDate.now())
        val attendanceMap = attendances.associate { it.user.id!! to it.type }
        return users.associate { it.id!! to (attendanceMap[it.id] ?: AttendanceType.NOT_ATTEND) }
    }
}
