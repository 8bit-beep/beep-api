package com.b.beep.domain.period.service

import com.b.beep.domain.period.controller.dto.request.CreatePeriodRequest
import com.b.beep.domain.period.controller.dto.request.UpdatePeriodRequest
import com.b.beep.domain.period.controller.dto.response.PeriodResponse
import com.b.beep.domain.period.entity.PeriodEntity
import com.b.beep.domain.period.error.PeriodError
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.ZoneId

@Service
@Transactional
class PeriodService(
    private val periodRepository: PeriodRepository,
) {
    @Transactional
    fun createPeriod(request: CreatePeriodRequest) {
        periodRepository.save(
            PeriodEntity(
                period = request.period,
                attendanceStartTime = request.attendanceStartTime,
                attendanceEndTime = request.attendanceEndTime,
                periodStartTime = request.periodStartTime,
                periodEndTime = request.periodEndTime,
            )
        )
    }

    @Transactional(readOnly = true)
    fun getPeriods(): List<PeriodResponse> {
        return periodRepository.findAll().map { PeriodResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getPeriod(periodId: Long): PeriodResponse {
        val period = periodRepository.findByIdOrNull(periodId)
            ?: throw CustomException(PeriodError.PERIOD_NOT_FOUND)
        return PeriodResponse.from(period)
    }

    @Transactional
    fun updatePeriod(periodId: Long, request: UpdatePeriodRequest) {
        val period = periodRepository.findByIdOrNull(periodId)
            ?: throw CustomException(PeriodError.PERIOD_NOT_FOUND)

        request.attendanceStartTime?.let { period.attendanceStartTime = it }
        request.attendanceEndTime?.let { period.attendanceEndTime = it }
        request.periodStartTime?.let { period.periodStartTime = it }
        request.periodEndTime?.let { period.periodEndTime = it }

        periodRepository.save(period)
    }

    @Transactional
    fun deletePeriod(periodId: Long) {
        val period = periodRepository.findByIdOrNull(periodId)
            ?: throw CustomException(PeriodError.PERIOD_NOT_FOUND)
        periodRepository.delete(period)
    }

    @Transactional(readOnly = true)
    fun getCurrentAttendancePeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val periods = periodRepository.findAll()

        for (period in periods) {
            if (now.isAfter(period.attendanceStartTime) && now.isBefore(period.attendanceEndTime)) {
                return period.period
            }
        }

        throw CustomException(PeriodError.TIME_UNAVAILABLE)
    }

    @Transactional(readOnly = true)
    fun getCurrentPeriod(): Int {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val periods = periodRepository.findAll()

        for (period in periods) {
            if (now.isAfter(period.periodStartTime) && now.isBefore(period.periodEndTime)) {
                return period.period
            }
        }

        return 0
    }
}
