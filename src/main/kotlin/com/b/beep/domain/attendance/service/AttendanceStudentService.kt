package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceStudentQueryRepository
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class AttendanceStudentService(
    private val attendanceStudentQueryRepository: AttendanceStudentQueryRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val attendanceRepository: AttendanceRepository
) {
    fun findAll(
        room: Room?,
        type: AttendanceType?,
        status: AttendanceType?,
        grade: Int?,
        cls: Int?
    ): List<AttendanceStudentResponse> {
        val users = attendanceStudentQueryRepository.findAllByFilters(
            room = room,
            type = type,
            status = status,
            grade = grade,
            cls = cls
        )

        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val schedules = studentScheduleRepository.findAllByUser(user)
            val attendances = attendanceRepository.findAllByUserAndDate(user, LocalDate.now())
            AttendanceStudentResponse.of(user, studentInfo, schedules, attendances)
        }
    }
}
