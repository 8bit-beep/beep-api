package com.b.beep.domain.user.service

import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class StudentScheduleValidator(
    private val studentScheduleRepository: StudentScheduleRepository,
    private val periodRepository: PeriodRepository
) {
    fun validateDayOfWeek(dayOfWeek: DayOfWeek) {
        val validDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        if (dayOfWeek !in validDays) {
            throw CustomException(StudentScheduleError.INVALID_DAY_OF_WEEK)
        }
    }

    fun validatePeriod(period: Int) {
        if (!periodRepository.existsByPeriod(period)) {
            throw CustomException(StudentScheduleError.INVALID_PERIOD)
        }
    }

    fun validateNotDuplicate(user: UserEntity, dayOfWeek: DayOfWeek, period: Int) {
        if (studentScheduleRepository.existsByUserAndDayOfWeekAndPeriod(user, dayOfWeek, period)) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }
    }

    fun validateNotDuplicateExcluding(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        period: Int,
        excludeScheduleId: Long
    ) {
        val existingSchedules = studentScheduleRepository.findAllByUser(user)
        val conflict = existingSchedules.any {
            it.id != excludeScheduleId &&
            it.dayOfWeek == dayOfWeek &&
            it.period == period
        }
        if (conflict) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }
    }
}
