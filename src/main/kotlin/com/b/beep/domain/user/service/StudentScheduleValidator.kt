package com.b.beep.domain.user.service

import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.common.SchedulePolicy
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class StudentScheduleValidator(
    private val studentScheduleRepository: StudentScheduleRepository,
    private val schedulePolicy: SchedulePolicy
) {
    fun validateDayOfWeek(dayOfWeek: DayOfWeek) {
        if (dayOfWeek !in schedulePolicy.validDays) {
            throw CustomException(StudentScheduleError.INVALID_DAY_OF_WEEK)
        }
    }

    fun validatePeriod(period: Int) {
        if (period !in schedulePolicy.validPeriods) {
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
