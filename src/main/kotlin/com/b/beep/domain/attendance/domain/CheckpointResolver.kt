package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.time.ZoneId

@Component
class CheckpointResolver(
    private val checkpointRepository: AttendanceCheckpointRepository
) {
    fun getCurrentCheckpoint(): AttendanceCheckpointEntity {
        return getCurrentCheckpointOrNull() ?: throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentCheckpointOrNull(): AttendanceCheckpointEntity? {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val checkpoints = checkpointRepository.findAllByIsDeletedFalse()

        for (checkpoint in checkpoints) {
            if (!now.isBefore(checkpoint.startAt) && now.isBefore(checkpoint.endAt)) {
                return checkpoint
            }
        }
        return null
    }

    fun getCurrentAttendableCheckpoint(): AttendanceCheckpointEntity {
        return getCurrentAttendableCheckpointOrNull() ?: throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentAttendableCheckpointOrNull(): AttendanceCheckpointEntity? {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val checkpoints = checkpointRepository.findAllByIsDeletedFalse()

        for (checkpoint in checkpoints) {
            val attendanceStartAt = checkpoint.attendanceStartAt
            val attendanceEndAt = checkpoint.attendanceEndAt

            if (!now.isBefore(attendanceStartAt) && now.isBefore(attendanceEndAt)) {
                return checkpoint
            }
        }
        return null
    }

    fun canAttend(): Boolean {
        return getCurrentAttendableCheckpointOrNull() != null
    }

    fun canAttend(checkpoint: AttendanceCheckpointEntity): Boolean {
        val attendanceStartAt = checkpoint.attendanceStartAt ?: return false
        val attendanceEndAt = checkpoint.attendanceEndAt ?: return false

        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        return !now.isBefore(attendanceStartAt) && now.isBefore(attendanceEndAt)
    }
}
