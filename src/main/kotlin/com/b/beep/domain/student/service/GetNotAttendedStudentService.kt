package com.b.beep.domain.student.service

import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.student.controller.dto.response.StudentResponse
import com.b.beep.domain.student.repository.StudentQueryRepository
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetNotAttendedStudentService(
    private val studentQueryRepository: StudentQueryRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val attendanceRepository: AttendanceRepository,
    private val periodResolver: PeriodResolver
) {
    fun getAll(type: AttendanceType): List<StudentResponse> {
        val today = LocalDate.now()
        val period = periodResolver.getCurrentPeriod()
        if (period == 0) return emptyList()

        val attendances = attendanceRepository.findAllByPeriodAndDateAndType(period, today, AttendanceType.NOT_ATTEND)
        val users = attendances.map { it.user }.filter { it.role == UserRole.STUDENT }

        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val schedules = studentScheduleRepository.findAllByUser(user)
            val userAttendances = attendanceRepository.findAllByUserAndDate(user, today)
            StudentResponse.of(user, studentInfo, schedules, userAttendances)
        }
    }

    fun getAllByRoomAndType(room: Room, type: AttendanceType): List<StudentResponse> {
        val users = studentQueryRepository.findAllByStatusAndRoomAndType(room, type, AttendanceType.NOT_ATTEND)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val schedules = studentScheduleRepository.findAllByUser(user)
            val attendances = attendanceRepository.findAllByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, schedules, attendances)
        }
    }

    fun getAllByGradeAndCls(grade: Int, cls: Int): List<StudentResponse> {
        val users = studentQueryRepository.findAllByStatusAndGradeAndCls(grade, cls, AttendanceType.NOT_ATTEND)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val schedules = studentScheduleRepository.findAllByUser(user)
            val attendances = attendanceRepository.findAllByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, schedules, attendances)
        }
    }
}
