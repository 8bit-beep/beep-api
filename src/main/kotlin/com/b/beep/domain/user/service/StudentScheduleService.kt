package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.CreateMyScheduleRequest
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.domain.user.domain.StudentScheduleValidator
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudentScheduleService(
    private val studentScheduleRepository: StudentScheduleRepository,
    private val studentScheduleValidator: StudentScheduleValidator,
    private val contextHolder: ContextHolder,
    private val roomRepository: RoomRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val attendanceTypeService: AttendanceTypeService,
    private val userRepository: UserRepository,
) {
    fun createSchedule(request: CreateStudentScheduleRequest) {
        val user = userRepository.findByIdAndIsDeletedFalse(request.userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
        val room = getRoomEntity(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val checkpoint = getCheckpointEntity(request.checkpointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)

        studentScheduleValidator.validateDayOfWeek(request.dayOfWeek)
        studentScheduleValidator.validateNotDuplicate(user, request.dayOfWeek, checkpoint)

        val schedule = StudentScheduleEntity(
            user = user,
            dayOfWeek = request.dayOfWeek,
            checkpoint = checkpoint,
            type = type,
            room = room
        )
        studentScheduleRepository.save(schedule)
    }

    fun createMySchedule(request: CreateMyScheduleRequest) {
        val user = contextHolder.user
        val room = getRoomEntity(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val checkpoint = getCheckpointEntity(request.checkpointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)

        studentScheduleValidator.validateDayOfWeek(request.dayOfWeek)
        studentScheduleValidator.validateNotDuplicate(user, request.dayOfWeek, checkpoint)

        val schedule = StudentScheduleEntity(
            user = user,
            dayOfWeek = request.dayOfWeek,
            checkpoint = checkpoint,
            type = type,
            room = room
        )
        studentScheduleRepository.save(schedule)
    }

    fun updateSchedule(scheduleId: Long, request: UpdateStudentScheduleRequest) {
        val schedule = getScheduleEntityOrThrow(scheduleId)

        request.dayOfWeek?.let { studentScheduleValidator.validateDayOfWeek(it) }

        val finalDayOfWeek = request.dayOfWeek ?: schedule.dayOfWeek
        val finalCheckpoint = request.checkpointId?.let {
            getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        } ?: schedule.checkpoint

        studentScheduleValidator.validateNotDuplicateExcluding(
            schedule.user, finalDayOfWeek, finalCheckpoint, scheduleId
        )

        request.dayOfWeek?.let { schedule.dayOfWeek = it }
        request.checkpointId?.let {
            schedule.checkpoint = getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        }
        request.typeId?.let { schedule.type = attendanceTypeService.getAttendanceTypeEntityById(it) }
        request.roomId?.let {
            schedule.room = getRoomEntity(it) ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        }

        studentScheduleRepository.save(schedule)
    }

    fun deleteSchedule(scheduleId: Long) {
        val schedule = getScheduleEntityOrThrow(scheduleId)
        studentScheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getMySchedules(): List<StudentScheduleResponse> {
        val user = contextHolder.user
        return studentScheduleRepository.findAllByUser(user).map { StudentScheduleResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getSchedulesByUserId(userId: Long): List<StudentScheduleResponse> {
        val user = userRepository.findByIdAndIsDeletedFalse(userId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
        return studentScheduleRepository.findAllByUser(user).map { StudentScheduleResponse.of(it) }
    }

    private fun getScheduleEntityOrThrow(scheduleId: Long): StudentScheduleEntity {
        return studentScheduleRepository.findByIdOrNull(scheduleId)
            ?: throw CustomException(StudentScheduleError.SCHEDULE_NOT_FOUND)
    }

    private fun getRoomEntity(roomId: Long): RoomEntity? {
        return roomRepository.findByIdOrNull(roomId)
    }

    private fun getCheckpointEntity(checkpointId: Long): AttendanceCheckpointEntity? {
        return checkpointRepository.findByIdOrNull(checkpointId)
    }
}
