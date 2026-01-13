package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.entity.AbsenceEntity
import com.b.beep.domain.absence.error.AbsenceError
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.entity.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class AbsenceService(
    private val absenceRepository: AbsenceRepository,
    private val studentInfoRepository: StudentInfoRepository
) {
    fun create(request: CreateAbsenceRequest) {
        val studentInfo = studentInfoRepository.findByGradeAndClsAndNum(request.grade, request.cls, request.num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        validateDateRange(request.startDate, request.endDate)

        // 같은 학생의 겹치는 날짜 범위 중복 체크
        val userId = studentInfo.user.id ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
        val hasOverlappingAbsence = absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            userId = userId,
            endDate = request.endDate,
            startDate = request.startDate
        )

        if (hasOverlappingAbsence) {
            throw CustomException(AbsenceError.ABSENCE_ALREADY_EXISTS)
        }

        absenceRepository.save(
            AbsenceEntity(
                user = studentInfo.user,
                startDate = request.startDate,
                endDate = request.endDate,
                reason = request.reason,
            )
        )
    }

    @Transactional(readOnly = true)
    fun getAll(): List<AbsenceEntity> {
        return absenceRepository.findAll()
    }

    fun update(id: Long, request: UpdateAbsenceRequest) {
        val absence = absenceRepository.findByIdOrNull(id)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        validateDateRange(request.startDate, request.endDate)

        absence.startDate = request.startDate
        absence.endDate = request.endDate
        absence.reason = request.reason

        absenceRepository.save(absence)
    }

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        if (startDate.isAfter(endDate)) {
            throw CustomException(AbsenceError.INVALID_DATE_RANGE)
        }
    }

    fun delete(id: Long) {
        val absence = absenceRepository.findByIdOrNull(id)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        absenceRepository.delete(absence)
    }

    @Transactional(readOnly = true)
    fun getUsersInLongAbsence(date: LocalDate): List<UserEntity> {
        val absences = absenceRepository.findAllByStartDateLessThanEqualAndEndDateGreaterThanEqualWithUser(date, date)
        return absences.map { it.user }
    }
}