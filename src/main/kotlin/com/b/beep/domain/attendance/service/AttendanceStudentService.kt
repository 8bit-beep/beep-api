package com.b.beep.domain.attendance.service

import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.controller.dto.response.ScheduleResponse
import com.b.beep.domain.attendance.controller.dto.response.StatusResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceStudentQueryRepository
import com.b.beep.domain.period.repository.PeriodRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
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
    private val attendanceRepository: AttendanceRepository,
    private val periodRepository: PeriodRepository,
    private val absenceRepository: AbsenceRepository,
    private val roomRepository: RoomRepository
) {
    fun findAll(
        roomId: Long?,
        type: AttendanceType?,
        status: AttendanceType?,
        grade: Int?,
        classNumber: Int?
    ): List<AttendanceStudentResponse> {
        val room = roomId?.let { roomRepository.findById(it).orElse(null) }
        val users = attendanceStudentQueryRepository.findAllByFilters(
            room = room,
            type = type,
            status = status,
            grade = grade,
            classNumber = classNumber
        )

        return users.map { it.toResponse() }
    }

    private fun UserEntity.toResponse(): AttendanceStudentResponse {
        val today = LocalDate.now()
        val studentInfo = studentInfoRepository.findByUser(this)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
        val schedules = studentScheduleRepository.findAllByUser(this)
        val attendances = attendanceRepository.findAllByUserAndDate(this, today)
        val periods = periodRepository.findAll()
        val isAbsent = absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            this.id!!, today, today
        )

        val attendanceMap = attendances.associateBy { it.period }
        val statuses = periods.map { period ->
            val type = attendanceMap[period.period]?.type
                ?: if (isAbsent) AttendanceType.OUTGOING else AttendanceType.NOT_ATTEND
            StatusResponse(period.period, type)
        }

        return AttendanceStudentResponse(
            username = this.username,
            studentId = generateStudentId(studentInfo),
            schedules = schedules.map { ScheduleResponse.of(it) },
            statuses = statuses
        )
    }

    private fun generateStudentId(studentInfo: StudentInfoEntity): String {
        return String.format("%d%d%02d", studentInfo.grade, studentInfo.classNumber, studentInfo.num)
    }
}
