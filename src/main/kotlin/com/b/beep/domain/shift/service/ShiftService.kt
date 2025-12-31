package com.b.beep.domain.shift.service

import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.domain.error.ShiftError
import com.b.beep.domain.shift.entity.ShiftEntity
import com.b.beep.domain.shift.repository.ShiftRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
@Transactional
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val contextHolder: ContextHolder,
) {
    @Transactional
    fun create(request: CreateShiftRequest) {
        val user = contextHolder.user

        if (shiftRepository.existsByUserAndDateAndPeriod(user, request.date, request.period))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        if (!isShiftTimeValid(request.date, request.period))
            throw CustomException(ShiftError.PASSED_TIME)

        val shift = ShiftEntity(
            user = user,
            room = request.room,
            period = request.period,
            reason = request.reason,
            status = ShiftStatus.WAITING,
            date = request.date,
        )
        shiftRepository.save(shift)
    }

    fun update(id: Long, request: UpdateShiftRequest) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        if (!isShiftTimeValid(request.date, request.period))
            throw CustomException(ShiftError.PASSED_TIME)

        request.reason?.let { shift.reason = it }
        request.date?.let { shift.date = it }
        request.room?.let { shift.room = it }
        request.period?.let { shift.period = it }

        if (shiftRepository.existsByUserAndDateAndPeriod(shift.user, shift.date, shift.period))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        // 변경 후 대기 중으로 변경
        shift.status = ShiftStatus.WAITING

        shiftRepository.save(shift)
    }

    fun delete(id: Long) {
        shiftRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getMyShifts(): List<ShiftEntity> {
        val user = contextHolder.user
        return shiftRepository.findAllByUserAndDate(user, LocalDate.now())
    }

    private fun isShiftTimeValid(date: LocalDate?, period: Int?): Boolean {
        val now = LocalDate.now()
        val currentTime = LocalTime.now(ZoneId.of("Asia/Seoul"))

        if (date == null) return true
        if (date.isBefore(now)) return false

        // 1교시, 2교시만 실이동 가능
        if (period != null && period !in listOf(1, 2)) {
            return false
        }

        // 오늘이면, 해당 교시 시작 시간 전까지만 신청 가능
        if (date.isEqual(now) && period != null) {
            val periodStartTime = when (period) {
                1 -> LocalTime.of(9, 0)
                2 -> LocalTime.of(13, 20)
                else -> return false
            }

            // 시작 시간이 지났으면 신청 불가
            if (currentTime >= periodStartTime) {
                return false
            }
        }

        return true
    }
}
