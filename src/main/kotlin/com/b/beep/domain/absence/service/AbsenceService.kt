package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.AbsenceStudentResponse
import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.error.AbsenceError
import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class AbsenceService(
    private val absenceRepository: AbsenceRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
) {
    fun createAbsence(request: CreateAbsenceRequest) {
        val studentInfo = getStudentInfoEntity(request.grade, request.classNumber, request.num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
        val userId = studentInfo.user.id
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        validateDateRange(request.startDate, request.endDate)

        if (existsOverlappingAbsence(userId, request.startDate, request.endDate)) {
            throw CustomException(AbsenceError.ABSENCE_ALREADY_EXISTS)
        }

        val absence = absenceRepository.save(
            AbsenceEntity(
                user = studentInfo.user,
                startDate = request.startDate,
                endDate = request.endDate,
                reason = request.reason,
            )
        )

        createAttendancesForAbsence(absence, studentInfo.user, request.startDate, request.endDate)
    }

    @Transactional(readOnly = true)
    fun getAbsences(): List<AbsenceResponse> {
        return absenceRepository.findAll().map { it.toResponse() }
    }

    fun updateAbsence(absenceId: Long, request: UpdateAbsenceRequest) {
        val absence = getAbsenceEntity(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)
        val userId = absence.user.id
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        validateDateRange(request.startDate, request.endDate)

        if (existsOverlappingAbsenceExcluding(userId, request.startDate, request.endDate, absenceId)) {
            throw CustomException(AbsenceError.ABSENCE_ALREADY_EXISTS)
        }

        val today = LocalDate.now()
        attendanceRepository.deleteAllByAbsenceAndDateGreaterThanEqual(absence, today)

        absence.startDate = request.startDate
        absence.endDate = request.endDate
        absence.reason = request.reason

        absenceRepository.save(absence)

        createAttendancesForAbsence(absence, absence.user, request.startDate, request.endDate)
    }

    fun deleteAbsence(absenceId: Long) {
        val absence = getAbsenceEntity(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        val today = LocalDate.now()
        attendanceRepository.deleteAllByAbsenceAndDateGreaterThanEqual(absence, today)

        absenceRepository.delete(absence)
    }

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        val today = LocalDate.now()
        if (startDate.isBefore(today)) {
            throw CustomException(AbsenceError.INVALID_DATE_RANGE)
        }
        if (startDate.isAfter(endDate)) {
            throw CustomException(AbsenceError.INVALID_DATE_RANGE)
        }
    }

    private fun createAttendancesForAbsence(
        absence: AbsenceEntity,
        user: UserEntity,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val today = LocalDate.now()
        var date = startDate
        while (!date.isAfter(endDate)) {
            if (!date.isBefore(today)) {
                val schedules = studentScheduleRepository.findAllByUserAndDayOfWeek(user, date.dayOfWeek)
                for (schedule in schedules) {
                    attendanceRepository.save(
                        AttendanceEntity(
                            user = user,
                            checkpoint = schedule.checkpoint,
                            date = date,
                            type = schedule.type,
                            room = schedule.room,
                            absence = absence
                        )
                    )
                }
            }
            date = date.plusDays(1)
        }
    }

    private fun existsOverlappingAbsence(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean {
        return absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            userId = userId,
            endDate = endDate,
            startDate = startDate
        )
    }

    private fun existsOverlappingAbsenceExcluding(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        excludeId: Long
    ): Boolean {
        return absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            userId = userId,
            endDate = endDate,
            startDate = startDate,
            id = excludeId
        )
    }

    private fun getStudentInfoEntity(grade: Int, classNumber: Int, num: Int): StudentInfoEntity? {
        return studentInfoRepository.findByGradeAndClassNumberAndNum(grade, classNumber, num)
    }

    private fun getAbsenceEntity(absenceId: Long): AbsenceEntity? {
        return absenceRepository.findByIdOrNull(absenceId)
    }

    private fun AbsenceEntity.toResponse(): AbsenceResponse {
        val studentInfo = studentInfoRepository.findByUser(user)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        return AbsenceResponse(
            absenceId = this.id!!,
            student = AbsenceStudentResponse(this.user.username, StudentInfoResponse.of(studentInfo)),
            startDate = this.startDate,
            endDate = this.endDate,
            reason = this.reason
        )
    }
}