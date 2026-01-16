package com.b.beep.domain.user.service

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.domain.StudentScheduleValidator
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.repository.StudentScheduleRepository
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
) {
    fun create(request: CreateStudentScheduleRequest) {
        val user = contextHolder.user
        val room = getRoomEntity(request.roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val checkpoint = getCheckpointEntity(request.checkpointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)

        studentScheduleValidator.validateDayOfWeek(request.dayOfWeek)
        studentScheduleValidator.validateNotDuplicate(user, request.dayOfWeek, checkpoint)

        val schedule = StudentScheduleEntity(
            user = user,
            dayOfWeek = request.dayOfWeek,
            checkpoint = checkpoint,
            type = request.type,
            room = room
        )
        studentScheduleRepository.save(schedule)
    }

    fun update(scheduleId: Long, request: UpdateStudentScheduleRequest) {
        val schedule = getScheduleOrThrow(scheduleId)
        val user = contextHolder.user

        validateOwnership(schedule, user)

        request.dayOfWeek?.let { studentScheduleValidator.validateDayOfWeek(it) }

        val finalDayOfWeek = request.dayOfWeek ?: schedule.dayOfWeek
        val finalCheckpoint = request.checkpointId?.let {
            getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        } ?: schedule.checkpoint

        studentScheduleValidator.validateNotDuplicateExcluding(
            user, finalDayOfWeek, finalCheckpoint, scheduleId
        )

        request.dayOfWeek?.let { schedule.dayOfWeek = it }
        request.checkpointId?.let {
            schedule.checkpoint = getCheckpointEntity(it) ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        }
        request.type?.let { schedule.type = it }
        request.roomId?.let {
            schedule.room = getRoomEntity(it) ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        }

        studentScheduleRepository.save(schedule)
    }

    fun delete(scheduleId: Long) {
        val schedule = getScheduleOrThrow(scheduleId)
        val user = contextHolder.user

        validateOwnership(schedule, user)

        studentScheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<StudentScheduleEntity> {
        val user = contextHolder.user
        return studentScheduleRepository.findAllByUser(user)
    }

    private fun getScheduleOrThrow(scheduleId: Long): StudentScheduleEntity {
        return studentScheduleRepository.findByIdOrNull(scheduleId)
            ?: throw CustomException(StudentScheduleError.SCHEDULE_NOT_FOUND)
    }

    private fun validateOwnership(schedule: StudentScheduleEntity, user: UserEntity) {
        if (schedule.user.id != user.id) {
            throw CustomException(StudentScheduleError.NO_PERMISSION)
        }
    }

    private fun getRoomEntity(roomId: Long): RoomEntity? {
        return roomRepository.findByIdOrNull(roomId)
    }

    private fun getCheckpointEntity(checkpointId: Long): AttendanceCheckpointEntity? {
        return checkpointRepository.findByIdOrNull(checkpointId)
    }
}
