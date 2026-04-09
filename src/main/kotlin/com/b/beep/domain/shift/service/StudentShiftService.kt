package com.b.beep.domain.shift.service

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.entity.ShiftEntity
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.error.ShiftError
import com.b.beep.domain.shift.repository.ShiftRepository
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
@Transactional
class StudentShiftService(
    private val shiftRepository: ShiftRepository,
    private val contextHolder: ContextHolder,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val roomRepository: RoomRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
) {
    @Transactional
    fun createShift(request: CreateShiftRequest) {
        val user = contextHolder.user
        val room = getRoomEntity(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val checkpoint = getCheckpointEntity(request.checkpointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)

        if (shiftRepository.existsByUserAndDateAndCheckpoint(user, request.date, checkpoint))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        if (!isShiftTimeValid(request.date, checkpoint.attendanceStartAt))
            throw CustomException(ShiftError.PASSED_TIME)

        val shift = ShiftEntity(
            user = user,
            room = room,
            checkpoint = checkpoint,
            reason = request.reason,
            status = ShiftStatus.WAITING,
            date = request.date,
        )

        try {
            shiftRepository.save(shift)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)
        }
    }

    @Transactional(readOnly = true)
    fun getMyShifts(): List<ShiftResponse> {
        val user = contextHolder.user
        return shiftRepository.findAllByUserAndDateOrderByCheckpointStartAtAsc(user, LocalDate.now(ZoneId.of("Asia/Seoul")))
            .map { it.toResponse() }
    }

    fun updateShift(id: Long, request: UpdateShiftRequest) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        val user = contextHolder.user
        if (shift.user.id != user.id) {
            throw CustomException(ShiftError.SHIFT_NOT_FOUND)
        }

        val newCheckpoint = request.checkpointId?.let {
            getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        } ?: shift.checkpoint
        val newDate = request.date ?: shift.date

        if (!isShiftTimeValid(newDate, newCheckpoint.attendanceStartAt))
            throw CustomException(ShiftError.PASSED_TIME)

        request.reason?.let { shift.reason = it }
        request.date?.let { shift.date = it }
        request.roomId?.let {
            shift.room = getRoomEntity(it) ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        }
        request.checkpointId?.let {
            shift.checkpoint = getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        }

        if (shiftRepository.existsByUserAndDateAndCheckpointAndIdNot(shift.user, shift.date, shift.checkpoint, id))
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)

        if (shift.status == ShiftStatus.APPROVED) {
            attendanceRepository.findByCheckpointAndUserAndDate(shift.checkpoint, shift.user, shift.date)
                ?.let { attendanceRepository.delete(it) }
        }

        shift.status = ShiftStatus.WAITING

        try {
            shiftRepository.save(shift)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ShiftError.SHIFT_ALREADY_EXISTS)
        }
    }

    fun deleteShift(id: Long) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        val user = contextHolder.user
        if (shift.user.id != user.id) {
            throw CustomException(ShiftError.SHIFT_NOT_FOUND)
        }

        shiftRepository.delete(shift)
    }

    private fun isShiftTimeValid(date: LocalDate?, attendanceStartAt: LocalTime?): Boolean {
        val now = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val currentTime = LocalTime.now(ZoneId.of("Asia/Seoul"))

        if (date == null) return true
        if (date.isBefore(now)) return false

        if (date.isEqual(now) && attendanceStartAt != null) {
            if (currentTime >= attendanceStartAt) {
                return false
            }
        }

        return true
    }

    private fun getRoomEntity(roomId: Long): RoomEntity? {
        return roomRepository.findByIdOrNull(roomId)
    }

    private fun getCheckpointEntity(checkpointId: Long): AttendanceCheckpointEntity? {
        return checkpointRepository.findByIdOrNull(checkpointId)
    }

    private fun ShiftEntity.toResponse(): ShiftResponse {
        val studentInfo = studentInfoRepository.findByUser(this.user)
        return ShiftResponse(
            id = id!!,
            user = UserResponse(
                id = user.id,
                username = user.username,
                name = user.name,
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
