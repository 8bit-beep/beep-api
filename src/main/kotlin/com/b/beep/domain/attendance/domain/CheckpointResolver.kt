package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
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

    fun getCurrentCheckpointOrNearest(): AttendanceCheckpointEntity {
        getCurrentCheckpointOrNull()?.let { return it }

        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val checkpoints = checkpointRepository.findAllByIsDeletedFalseOrderByStartAtAsc()
        if (checkpoints.isEmpty()) throw CustomException(AttendanceError.TIME_UNAVAILABLE)

        val first = checkpoints.first()
        val last = checkpoints.last()

        if (now.isBefore(first.startAt)) return first
        if (!now.isBefore(last.endAt)) return last

        for (i in checkpoints.indices) {
            val current = checkpoints[i]
            if (i < checkpoints.lastIndex) {
                val next = checkpoints[i + 1]
                if (!now.isBefore(current.endAt) && now.isBefore(next.startAt)) {
                    return next
                }
            }
        }

        throw CustomException(AttendanceError.TIME_UNAVAILABLE)
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

    fun getCurrentAttendableCheckpoint(grade: Int): AttendanceCheckpointEntity {
        return getCurrentAttendableCheckpointOrNull(grade) ?: throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentAttendableCheckpointOrNull(grade: Int): AttendanceCheckpointEntity? {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val today = LocalDate.now(ZoneId.of("Asia/Seoul")).dayOfWeek
        val checkpoints = filterForStudent(checkpointRepository.findAllByIsDeletedFalse(), grade, today)

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

    // [주의] 학년/요일 전용 체크포인트와 일반 체크포인트의 겹침은 startAt~endAt 기준으로 판단합니다.
    // 현재 1학년 월요일 전용: 7~8교시(15:20~17:19), 9교시(17:20~18:10)
    // 현재 일반 체크포인트: 8~9교시(16:30~18:59)
    // 추후 시간 변경 시 전용 체크포인트의 startAt~endAt이 일반 체크포인트와 반드시 겹쳐야
    // 1학년 월요일에서 일반 체크포인트(8~9교시)가 정상적으로 제외됩니다.
    private fun filterForStudent(
        checkpoints: List<AttendanceCheckpointEntity>,
        grade: Int,
        dayOfWeek: DayOfWeek
    ): List<AttendanceCheckpointEntity> {
        val specific = checkpoints.filter { it.grade == grade && it.dayOfWeek == dayOfWeek }
        val general = checkpoints.filter { it.grade == null && it.dayOfWeek == null }

        val filteredGeneral = general.filter { gen ->
            specific.none { spec ->
                spec.startAt.isBefore(gen.endAt) && gen.startAt.isBefore(spec.endAt)
            }
        }

        return specific + filteredGeneral
    }
}
