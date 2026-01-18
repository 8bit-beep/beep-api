package com.b.beep.domain.checkpoint.service

import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.shift.repository.ShiftRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@Service
@Transactional
class AttendanceCheckpointService(
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val attendanceRepository: AttendanceRepository,
    private val shiftRepository: ShiftRepository,
    private val studentScheduleRepository: StudentScheduleRepository
) {
    fun createCheckpoint(request: CreateCheckpointRequest) {
        validateTimeOverlap(request.startAt, request.endAt, excludeId = null)

        checkpointRepository.save(
            AttendanceCheckpointEntity(
                name = request.name,
                startAt = request.startAt,
                endAt = request.endAt,
                attendanceStartAt = request.attendanceStartAt,
                attendanceEndAt = request.attendanceEndAt
        ))
    }

    @Transactional(readOnly = true)
    fun getCheckpoints(): List<CheckpointResponse> {
        return checkpointRepository.findAllByIsDeletedFalse().map { CheckpointResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getCheckpoint(checkPointId: Long): CheckpointResponse {
        val entity = checkpointRepository.findByIdAndIsDeletedFalse(checkPointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)

        return CheckpointResponse.of(entity)
    }

    fun updateCheckpoint(checkPointId: Long, request: UpdateCheckpointRequest) {
        val checkpoint = checkpointRepository.findByIdAndIsDeletedFalse(checkPointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)

        val newStartAt = request.startAt ?: checkpoint.startAt
        val newEndAt = request.endAt ?: checkpoint.endAt
        validateTimeOverlap(newStartAt, newEndAt, excludeId = checkPointId)

        request.name?.let { checkpoint.name = it }
        request.startAt?.let { checkpoint.startAt = it }
        request.endAt?.let { checkpoint.endAt = it }
        request.attendanceStartAt?.let { checkpoint.attendanceStartAt = it }
        request.attendanceEndAt?.let { checkpoint.attendanceEndAt = it }

        checkpointRepository.save(checkpoint)
    }

    fun deleteCheckpoint(checkPointId: Long) {
        val checkpoint = checkpointRepository.findByIdAndIsDeletedFalse(checkPointId)
            ?: throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)

        if (isCheckpointInUse(checkpoint)) {
            throw CustomException(CheckpointError.CHECKPOINT_IN_USE)
        }

        checkpoint.isDeleted = true
    }

    private fun isCheckpointInUse(checkpoint: AttendanceCheckpointEntity): Boolean {
        return attendanceRepository.existsByCheckpoint(checkpoint) ||
                shiftRepository.existsByCheckpoint(checkpoint) ||
                studentScheduleRepository.existsByCheckpoint(checkpoint)
    }

    private fun validateTimeOverlap(startAt: LocalTime, endAt: LocalTime, excludeId: Long?) {
        val existing = checkpointRepository.findAllByIsDeletedFalse()
            .filter { excludeId == null || it.id != excludeId }

        for (checkpoint in existing) {
            if (startAt < checkpoint.endAt && checkpoint.startAt < endAt) {
                throw CustomException(CheckpointError.CHECKPOINT_TIME_OVERLAP)
            }
        }
    }

    fun getCheckpointEntity(checkPointId: Long): AttendanceCheckpointEntity? {
        return checkpointRepository.findByIdOrNull(checkPointId)
    }
}
