package com.b.beep.domain.period.controller.dto.response

import com.b.beep.domain.period.entity.PeriodEntity
import java.time.LocalTime

data class PeriodResponse(
    val id: Long,
    val period: Int,
    val attendanceStartTime: LocalTime,
    val attendanceEndTime: LocalTime,
    val periodStartTime: LocalTime,
    val periodEndTime: LocalTime,
) {
    companion object {
        fun from(entity: PeriodEntity): PeriodResponse {
            return PeriodResponse(
                id = entity.id!!,
                period = entity.period,
                attendanceStartTime = entity.attendanceStartTime,
                attendanceEndTime = entity.attendanceEndTime,
                periodStartTime = entity.periodStartTime,
                periodEndTime = entity.periodEndTime,
            )
        }
    }
}
