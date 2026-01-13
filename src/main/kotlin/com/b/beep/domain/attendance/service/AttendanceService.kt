package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.AttendRequest
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Service
@Transactional
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val contextHolder: ContextHolder,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val periodResolver: PeriodResolver,
) {
    @Transactional
    fun attend(request: AttendRequest) {
        val user = contextHolder.user
        val period = periodResolver.getCurrentAttendancePeriod()
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek

        val schedule = studentScheduleRepository.findByUserAndDayOfWeekAndPeriodAndType(
            user, dayOfWeek, period, request.attendanceType
        )

        if (schedule == null) {
            throw CustomException(AttendanceError.SCHEDULE_NOT_FOUND)
        }

        if (schedule.room != request.room) {
            throw CustomException(AttendanceError.ROOM_MISMATCH)
        }

        val attendance = attendanceRepository.findByUserIdAndPeriodAndDate(user.id!!, period, LocalDate.now())
            ?: throw CustomException(AttendanceError.ATTENDANCE_NOT_FOUND)

        if (attendance.type != AttendanceType.NOT_ATTEND) {
            throw CustomException(AttendanceError.ALREADY_ATTENDED)
        }

        attendance.type = request.attendanceType
        attendance.room = request.room

        attendanceRepository.save(attendance)
    }

    @Transactional
    fun cancelAttendance() {
        val user = contextHolder.user
        val period = periodResolver.getCurrentAttendancePeriod()

        val attendance = attendanceRepository.findByUserIdAndPeriodAndDate(user.id!!, period, LocalDate.now())
            ?: throw CustomException(AttendanceError.ATTENDANCE_NOT_FOUND)

        attendance.type = AttendanceType.NOT_ATTEND
        attendance.room = null

        attendanceRepository.save(attendance)
    }
}
