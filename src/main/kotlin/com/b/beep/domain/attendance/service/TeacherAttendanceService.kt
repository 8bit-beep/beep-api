package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.controller.dto.response.StatusResponse
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class TeacherAttendanceService(
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val attendanceQueryRepository: AttendanceQueryRepository,
    private val roomRepository: RoomRepository,
    private val attendanceTypeService: AttendanceTypeService,
    private val userRepository: UserRepository
) {
    fun updateStudentStatus(request: UpdateStatusRequest) {
        val user = userRepository.findByIdAndIsDeletedFalse(request.userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        val status = attendanceTypeService.getAttendanceTypeEntityById(request.statusId)
        val targetDate = request.date ?: LocalDate.now(ZoneId.of("Asia/Seoul"))
        val targetCheckpoint = request.checkpointId?.let { checkpointRepository.findByIdOrNull(it) }
            ?: checkpointResolver.getCurrentCheckpoint()
        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(targetCheckpoint, user, targetDate)

        if (status.name == AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME) {
            attendance?.let {
                if (it.absence == null) {
                    attendanceRepository.delete(it)
                }
            }
            return
        }

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

    fun getAttendances(
        date: LocalDate?,
        checkpointId: Long?,
        roomId: Long?,
        statusId: Long?,
        grade: Int?,
        classNumber: Int?,
        isCurrentCheckpoint: Boolean = true
    ): List<AttendanceStudentResponse> {
        val checkpoint = checkpointId?.let {
            checkpointRepository.findByIdAndIsDeletedFalse(it)
                ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        }
        val room = roomId?.let {
            roomRepository.findByIdAndIsDeletedFalse(it) ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        }
        val status = statusId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) }
        val users = attendanceQueryRepository.findAllByFilters(
            date = date,
            checkpoint = checkpoint,
            room = room,
            status = status,
            grade = grade,
            classNumber = classNumber,
            isCurrentCheckpoint = isCurrentCheckpoint
        )

        return users.map { it.toResponse() }
    }

    private fun UserEntity.toResponse(): AttendanceStudentResponse {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val studentInfo = studentInfoRepository.findByUser(this)
            ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
        val attendances = attendanceRepository.findAllByUserAndDate(this, today)
        val checkpoints = checkpointRepository.findAllByIsDeletedFalse()

        val attendanceMap = attendances.associateBy { it.checkpoint.id }
        val statuses = checkpoints.map { checkpoint ->
            val type = attendanceMap[checkpoint.id]?.type
            StatusResponse(CheckpointSimpleResponse.of(checkpoint), type?.let { AttendanceTypeResponse.of(it) })
        }

        return AttendanceStudentResponse(
            userId = this.id!!,
            username = this.username,
            studentId = generateStudentId(studentInfo),
            statuses = statuses
        )
    }

    private fun generateStudentId(studentInfo: StudentInfoEntity): String {
        return String.format("%d%d%02d", studentInfo.grade, studentInfo.classNumber, studentInfo.num)
    }
}
