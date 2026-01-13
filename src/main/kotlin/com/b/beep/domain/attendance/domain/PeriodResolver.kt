package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.global.common.SchedulePolicy
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.time.ZoneId

@Component
class PeriodResolver(
    private val schedulePolicy: SchedulePolicy
) {
    fun getCurrentAttendancePeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))

        for (period in schedulePolicy.validPeriods) {
            val start = schedulePolicy.getAttendanceStartTime(period) ?: continue
            val end = schedulePolicy.getAttendanceEndTime(period) ?: continue
            if (now.isAfter(start) && now.isBefore(end)) {
                return period
            }
        }
        throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentPeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))

        for (period in schedulePolicy.validPeriods) {
            val start = schedulePolicy.getPeriodStartTime(period) ?: continue
            val end = schedulePolicy.getPeriodEndTime(period) ?: continue
            if (now.isAfter(start) && now.isBefore(end)) {
                return period
            }
        }
        return 0
    }
}
