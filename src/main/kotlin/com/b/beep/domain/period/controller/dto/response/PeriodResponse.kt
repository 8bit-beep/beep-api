package com.b.beep.domain.period.controller.dto.response

import com.b.beep.domain.period.domain.entity.PeriodEntity
import java.time.LocalTime

data class PeriodResponse(
    val period: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val attendStartTime: LocalTime,
    val attendEndTime: LocalTime
) {
    companion object {
        fun of(entity: PeriodEntity): PeriodResponse {
            return PeriodResponse(
                period = entity.period,
                startTime = entity.startTime,
                endTime = entity.endTime,
                attendStartTime = entity.attendStartTime,
                attendEndTime = entity.attendEndTime
            )
        }
    }
}
