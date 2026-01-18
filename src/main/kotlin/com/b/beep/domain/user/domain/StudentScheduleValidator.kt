package com.b.beep.domain.user.domain

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class StudentScheduleValidator(
    private val studentScheduleRepository: StudentScheduleRepository
) {
    fun validateDayOfWeek(dayOfWeek: DayOfWeek) {
        val validDays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
        if (dayOfWeek !in validDays) {
            throw CustomException(StudentScheduleError.INVALID_DAY_OF_WEEK)
        }
    }

    fun validateNotDuplicate(user: UserEntity, dayOfWeek: DayOfWeek, checkpoint: AttendanceCheckpointEntity) {
        if (studentScheduleRepository.existsByUserAndDayOfWeekAndCheckpoint(user, dayOfWeek, checkpoint)) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }
    }

    fun validateNotDuplicateExcluding(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity,
        excludeScheduleId: Long
    ) {
        val existingSchedules = studentScheduleRepository.findAllByUser(user)
        val conflict = existingSchedules.any {
            it.id != excludeScheduleId &&
                    it.dayOfWeek == dayOfWeek &&
                    it.checkpoint.id == checkpoint.id
        }
        if (conflict) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }
    }
}