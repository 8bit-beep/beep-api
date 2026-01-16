package com.b.beep.domain.attendance.service

import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.controller.dto.response.ScheduleResponse
import com.b.beep.domain.attendance.controller.dto.response.StatusResponse
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.repository.RoomRepository
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
class TeacherAttendanceService(
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val attendanceQueryRepository: AttendanceQueryRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val absenceRepository: AbsenceRepository,
    private val roomRepository: RoomRepository
) {
    fun updateStudentStatus(
        grade: Int,
        classNumber: Int,
        num: Int,
        status: AttendanceType,
        date: LocalDate? = null,
        checkpointId: Long? = null
    ) {
        val studentInfo = studentInfoRepository.findByGradeAndClassNumberAndNum(grade, classNumber, num)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)

        val user = studentInfo.user
        val targetDate = date ?: LocalDate.now()
        val targetCheckpoint = checkpointId?.let { checkpointRepository.findByIdOrNull(it) }
            ?: checkpointResolver.getCurrentCheckpoint()
        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(targetCheckpoint, user, targetDate)

        if (attendance != null) {
            attendance.type = status
            attendanceRepository.save(attendance)
        } else {
            attendanceRepository.save(
                AttendanceEntity(
                    user = user,
                    checkpoint = targetCheckpoint,
                    type = status,
                    room = null,
                    date = targetDate
                )
            )
        }
    }

    fun findAll(
        roomId: Long?,
        type: AttendanceType?,
        status: AttendanceType?,
        grade: Int?,
        classNumber: Int?
    ): List<AttendanceStudentResponse> {
        val room = roomId?.let { roomRepository.findById(it).orElse(null) }
        val users = attendanceQueryRepository.findAllByFilters(
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
        val checkpoints = checkpointRepository.findAll()
        val isAbsent = absenceRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            this.id!!, today, today
        )

        val attendanceMap = attendances.associateBy { it.checkpoint.id }
        val statuses = checkpoints.map { checkpoint ->
            val type = attendanceMap[checkpoint.id]?.type
                ?: if (isAbsent) AttendanceType.OUTGOING else AttendanceType.NOT_ATTEND
            StatusResponse(checkpoint.id!!, checkpoint.name, type)
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
