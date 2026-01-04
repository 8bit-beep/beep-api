package com.b.beep.domain.student.service

import com.b.beep.domain.attendance.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.period.service.PeriodService
import com.b.beep.domain.user.domain.UserError
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class StudentManagementService(
    private val studentInfoRepository: StudentInfoRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val periodService: PeriodService,
) {
    fun createPresetAttendance(grade: Int, cls: Int, num: Int, status: AttendanceType, period: Int) {
        val studentInfo = studentInfoRepository.findByGradeAndClsAndNum(grade, cls, num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        val user = studentInfo.user

        if (period == periodService.getCurrentPeriod()) {
            user.currentStatus = status
            userRepository.save(user)
        }

        val attendance = attendanceRepository.findByPeriodAndUserAndDate(period, user, LocalDate.now())

        if (attendance != null) {
            attendance.type = status
            attendanceRepository.save(attendance)
        } else {
            attendanceRepository.save(
                AttendanceEntity(
                    user = user,
                    period = period,
                    type = status,
                    room = null,
                    date = LocalDate.now()
                )
            )
        }
    }

    fun updateCurrentStudentStatus(grade: Int, cls: Int, num: Int, status: AttendanceType) {
        val studentInfo = studentInfoRepository.findByGradeAndClsAndNum(grade, cls, num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        val user = studentInfo.user

        user.currentStatus = status
        userRepository.save(user)

        val period = periodService.getCurrentPeriod()
        val attendance = attendanceRepository.findByPeriodAndUserAndDate(period, user, LocalDate.now())

        if (attendance != null) {
            attendance.type = status
            attendanceRepository.save(attendance)
        } else {
            attendanceRepository.save(
                AttendanceEntity(
                    user = user,
                    period = period,
                    type = status,
                    room = null,
                    date = LocalDate.now()
                )
            )
        }
    }
}
