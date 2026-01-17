package com.b.beep.domain.shift.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.entity.ShiftEntity
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.error.ShiftError
import com.b.beep.domain.shift.repository.ShiftRepository
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class TeacherShiftService(
    private val shiftRepository: ShiftRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceTypeService: AttendanceTypeService,
) {
    @Transactional(readOnly = true)
    fun getAll(): List<ShiftResponse> {
        return shiftRepository.findAllByDateGreaterThanEqual(LocalDate.now()).map { it.toResponse() }
    }

    fun updateStatus(id: Long, status: ShiftStatus) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        shift.status = status

        createAttendance(shift.checkpoint, shift.user, shift.room, shift.date, status)

        shiftRepository.save(shift)
    }

    private fun createAttendance(
        checkpoint: AttendanceCheckpointEntity,
        user: UserEntity,
        room: RoomEntity,
        date: LocalDate,
        status: ShiftStatus
    ) {
        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, date)
        val shiftAttendType = attendanceTypeService.getByName("SHIFT_ATTEND")

        when (status) {
            ShiftStatus.APPROVED -> {
                val record = attendance ?: AttendanceEntity(
                    user = user,
                    checkpoint = checkpoint,
                    date = date,
                    type = shiftAttendType,
                    room = room
                )
                record.type = shiftAttendType
                record.room = room
                attendanceRepository.save(record)
            }

            ShiftStatus.WAITING, ShiftStatus.REJECTED -> {
                attendance?.let { attendanceRepository.delete(it) }
            }
        }
    }

    private fun ShiftEntity.toResponse(): ShiftResponse {
        val studentInfo = studentInfoRepository.findByUser(this.user)
        return ShiftResponse(
            id = id!!,
            user = UserResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                role = user.role,
                profileImage = user.profileImage,
                studentInfo = studentInfo?.let { StudentInfoResponse.of(it) }
            ),
            room = RoomResponse.of(room),
            checkpoint = CheckpointSimpleResponse.of(checkpoint),
            reason = reason,
            status = status,
            date = date
        )
    }
}
