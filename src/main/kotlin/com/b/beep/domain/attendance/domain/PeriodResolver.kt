package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.time.ZoneId

@Component
class PeriodResolver(
    private val periodRepository: PeriodRepository
) {
    fun getCurrentAttendancePeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val periods = periodRepository.findAll()

        for (period in periods) {
            if (!now.isBefore(period.attendStartTime) && now.isBefore(period.attendEndTime)) {
                return period.period
            }
        }
        throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentPeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val periods = periodRepository.findAll()

        for (period in periods) {
            if (!now.isBefore(period.startTime) && now.isBefore(period.endTime)) {
                return period.period
            }
        }
        return 0
    }
}
