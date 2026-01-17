package com.b.beep.domain.absence.domain

import com.b.beep.domain.absence.error.AbsenceError
import com.b.beep.domain.absence.repository.AbsenceUserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AbsenceValidator(
    private val absenceUserRepository: AbsenceUserRepository
) {
    fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        val today = LocalDate.now()
        if (startDate.isBefore(today)) {
            throw CustomException(AbsenceError.INVALID_DATE_RANGE)
        }
        if (startDate.isAfter(endDate)) {
            throw CustomException(AbsenceError.INVALID_DATE_RANGE)
        }
    }

    fun existsOverlappingAbsence(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean {
        return absenceUserRepository.existsByUserIdAndAbsenceStartDateLessThanEqualAndAbsenceEndDateGreaterThanEqual(
            userId = userId,
            endDate = endDate,
            startDate = startDate
        )
    }

    fun existsOverlappingAbsenceExcluding(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        excludeId: Long
    ): Boolean {
        return absenceUserRepository.existsByUserIdAndAbsenceStartDateLessThanEqualAndAbsenceEndDateGreaterThanEqualAndAbsenceIdNot(
            userId = userId,
            endDate = endDate,
            startDate = startDate,
            absenceId = excludeId
        )
    }
}
