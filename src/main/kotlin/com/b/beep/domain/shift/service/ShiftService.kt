package com.b.beep.domain.shift.service

import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.error.ShiftError
import com.b.beep.domain.shift.domain.entity.ShiftEntity
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
    private val periodRepository: PeriodRepository,
    private val roomService: RoomService,
) {
    @Transactional
    fun create(request: CreateShiftRequest) {
        val user = contextHolder.user
        val room = roomService.getRoomById(request.roomId)

        if (shiftRepository.existsByUserAndDateAndPeriod(user, request.date, request.period))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        if (!isShiftTimeValid(request.date, request.period))
            throw CustomException(ShiftError.PASSED_TIME)

        val shift = ShiftEntity(
            user = user,
            room = room,
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

        val user = contextHolder.user
        if (shift.user.id != user.id) {
            throw CustomException(ShiftError.SHIFT_NOT_FOUND)
        }

        if (!isShiftTimeValid(request.date, request.period))
            throw CustomException(ShiftError.PASSED_TIME)

        request.reason?.let { shift.reason = it }
        request.date?.let { shift.date = it }
        request.roomId?.let { shift.room = roomService.getRoomById(it) }
        request.period?.let { shift.period = it }

        if (shiftRepository.existsByUserAndDateAndPeriodAndIdNot(shift.user, shift.date, shift.period, id))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        shift.status = ShiftStatus.WAITING

        shiftRepository.save(shift)
    }

    fun delete(id: Long) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        val user = contextHolder.user
        if (shift.user.id != user.id) {
            throw CustomException(ShiftError.SHIFT_NOT_FOUND)
        }

        shiftRepository.delete(shift)
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

        if (period != null && !periodRepository.existsByPeriod(period)) {
            return false
        }

        if (date.isEqual(now) && period != null) {
            val periodEntity = periodRepository.findByPeriod(period) ?: return false

            if (currentTime >= periodEntity.startTime) {
                return false
            }
        }

        return true
    }
}
