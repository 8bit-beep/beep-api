package com.b.beep.domain.period.service

import com.b.beep.domain.notification.scheduler.DynamicNotificationScheduler
import com.b.beep.domain.period.controller.dto.request.CreatePeriodRequest
import com.b.beep.domain.period.controller.dto.request.UpdatePeriodRequest
import com.b.beep.domain.period.domain.entity.PeriodEntity
import com.b.beep.domain.period.error.PeriodError
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodService(
    private val periodRepository: PeriodRepository,
    private val dynamicNotificationScheduler: DynamicNotificationScheduler
) {
    fun create(request: CreatePeriodRequest) {
        if (periodRepository.existsByPeriod(request.period)) {
            throw CustomException(PeriodError.PERIOD_ALREADY_EXISTS)
        }

        val period = PeriodEntity(
            period = request.period,
            startTime = request.startTime,
            endTime = request.endTime,
            attendStartTime = request.attendStartTime,
            attendEndTime = request.attendEndTime
        )
        periodRepository.save(period)
        dynamicNotificationScheduler.scheduleAllNotifications()
    }

    fun update(period: Int, request: UpdatePeriodRequest) {
        val entity = getByPeriod(period)
        entity.startTime = request.startTime
        entity.endTime = request.endTime
        entity.attendStartTime = request.attendStartTime
        entity.attendEndTime = request.attendEndTime
        periodRepository.save(entity)
        dynamicNotificationScheduler.scheduleAllNotifications()
    }

    fun delete(period: Int) {
        if (!periodRepository.existsByPeriod(period)) {
            throw CustomException(PeriodError.PERIOD_NOT_FOUND)
        }
        periodRepository.deleteByPeriod(period)
        dynamicNotificationScheduler.scheduleAllNotifications()
    }

    @Transactional(readOnly = true)
    fun getByPeriod(period: Int): PeriodEntity {
        return periodRepository.findByPeriod(period)
            ?: throw CustomException(PeriodError.PERIOD_NOT_FOUND)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<PeriodEntity> {
        return periodRepository.findAll()
    }
}
