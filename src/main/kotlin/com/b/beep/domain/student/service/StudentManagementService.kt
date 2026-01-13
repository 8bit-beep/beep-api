package com.b.beep.domain.student.service

import com.b.beep.domain.attendance.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.user.error.UserError
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class StudentManagementService(
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val periodResolver: PeriodResolver
) {
    fun createPresetAttendance(grade: Int, cls: Int, num: Int, status: AttendanceType, period: Int) {
        val studentInfo = studentInfoRepository.findByGradeAndClsAndNum(grade, cls, num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        val user = studentInfo.user
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
        val period = periodResolver.getCurrentPeriod()
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
