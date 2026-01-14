package com.b.beep.global.common

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime

interface SchedulePolicy {
    val validDays: Set<DayOfWeek>
    val validPeriods: Set<Int>
    val shiftablePeriods: Set<Int>

    fun getPeriodStartTime(period: Int): LocalTime?
    fun getPeriodEndTime(period: Int): LocalTime?
    fun getAttendanceStartTime(period: Int): LocalTime?
    fun getAttendanceEndTime(period: Int): LocalTime?
}

@Component
class DefaultSchedulePolicy : SchedulePolicy {
    override val validDays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY
    )

    override val validPeriods: Set<Int> = setOf(1, 2, 3)

    override val shiftablePeriods: Set<Int> = setOf(1, 2)

    private val periodTimes = mapOf(
        1 to PeriodTime(
            start = LocalTime.of(9, 0),
            end = LocalTime.of(13, 19, 59),
            attendanceStart = LocalTime.of(9, 0),
            attendanceEnd = LocalTime.of(9, 20)
        ),
        2 to PeriodTime(
            start = LocalTime.of(13, 20),
            end = LocalTime.of(18, 49, 59),
            attendanceStart = LocalTime.of(13, 20),
            attendanceEnd = LocalTime.of(13, 40)
        ),
        3 to PeriodTime(
            start = LocalTime.of(18, 50),
            end = LocalTime.of(21, 40),
            attendanceStart = LocalTime.of(18, 50),
            attendanceEnd = LocalTime.of(19, 10)
        )
    )

    override fun getPeriodStartTime(period: Int): LocalTime? = periodTimes[period]?.start

    override fun getPeriodEndTime(period: Int): LocalTime? = periodTimes[period]?.end

    override fun getAttendanceStartTime(period: Int): LocalTime? = periodTimes[period]?.attendanceStart

    override fun getAttendanceEndTime(period: Int): LocalTime? = periodTimes[period]?.attendanceEnd

    private data class PeriodTime(
        val start: LocalTime,
        val end: LocalTime,
        val attendanceStart: LocalTime,
        val attendanceEnd: LocalTime
    )
}
