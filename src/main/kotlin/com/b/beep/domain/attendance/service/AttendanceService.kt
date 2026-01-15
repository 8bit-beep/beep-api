package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.AttendRequest
import com.b.beep.domain.attendance.domain.PeriodResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val contextHolder: ContextHolder,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val periodResolver: PeriodResolver,
    private val roomService: RoomService,
) {
    @Transactional
    fun attend(request: AttendRequest) {
        val user = contextHolder.user
        val period = periodResolver.getCurrentAttendancePeriod()
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek
        val room = roomService.getRoomById(request.roomId)

        val schedule = studentScheduleRepository.findByUserAndDayOfWeekAndPeriodAndType(
            user, dayOfWeek, period, request.attendanceType
        )

        if (schedule == null) {
            throw CustomException(AttendanceError.SCHEDULE_NOT_FOUND)
        }

        if (schedule.room.id != room.id) {
            throw CustomException(AttendanceError.ROOM_MISMATCH)
        }

        val attendance = attendanceRepository.findByUserIdAndPeriodAndDate(user.id!!, period, today)
            ?: attendanceRepository.save(
                AttendanceEntity(
                    user = user,
                    period = period,
                    date = today,
                    type = AttendanceType.NOT_ATTEND
                )
            )

        if (attendance.type != AttendanceType.NOT_ATTEND) {
            throw CustomException(AttendanceError.ALREADY_ATTENDED)
        }

        attendance.type = request.attendanceType
        attendance.room = room

        attendanceRepository.save(attendance)
    }

    @Transactional
    fun cancelAttendance() {
        val user = contextHolder.user
        val period = periodResolver.getCurrentAttendancePeriod()

        val attendance = attendanceRepository.findByUserIdAndPeriodAndDate(user.id!!, period, LocalDate.now())
            ?: return

        attendanceRepository.delete(attendance)
    }
}
