package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.domain.error.AttendanceError
import com.b.beep.global.exception.CustomException
import java.time.LocalTime
import java.time.ZoneId

object PeriodResolver {
    fun getCurrentAttendancePeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))

        return if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(9, 20))) 1
        else if (now.isAfter(LocalTime.of(13, 20)) && now.isBefore(LocalTime.of(13, 40))) 2
        else if (now.isAfter(LocalTime.of(19, 0)) && now.isBefore(LocalTime.of(19, 20))) 3
        else throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentPeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))

        return if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(13, 19, 59))) 1
        else if (now.isAfter(LocalTime.of(13, 20)) && now.isBefore(LocalTime.of(18, 59, 59))) 2
        else if (now.isAfter(LocalTime.of(19, 0)) && now.isBefore(LocalTime.of(21, 40))) 3
        else 0
    }
}
