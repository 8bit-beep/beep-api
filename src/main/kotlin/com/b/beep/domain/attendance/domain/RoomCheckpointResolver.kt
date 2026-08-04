package com.b.beep.domain.attendance.domain

import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RoomCheckpointResolver(
    private val checkpointResolver: CheckpointResolver,
    private val attendanceQueryRepository: AttendanceQueryRepository,
    private val attendanceSortModeRepository: AttendanceSortModeRepository
) {
    fun getCurrentCheckpointOrNearest(
        date: LocalDate,
        room: RoomEntity?,
        requestedGrade: Int?
    ): AttendanceCheckpointEntity {
        val dayOfWeek = date.dayOfWeek
        val grade = requestedGrade ?: room?.grade

        if (grade != null) {
            return checkpointResolver.getCurrentCheckpointOrNearest(grade, dayOfWeek)
        }

        val generalCheckpoint = checkpointResolver.getCurrentCheckpointOrNearest()
        if (room != null) {
            val firstGradeCheckpoint = checkpointResolver.getCurrentCheckpointOrNearest(FIRST_GRADE, dayOfWeek)
            val roomId = room.id
            if (roomId != null &&
                firstGradeCheckpoint.id != generalCheckpoint.id &&
                roomId in findFirstGradeRoomIds(date, firstGradeCheckpoint)
            ) {
                return firstGradeCheckpoint
            }
        }

        return generalCheckpoint
    }

    fun getCurrentCheckpoint(
        date: LocalDate,
        room: RoomEntity
    ): AttendanceCheckpointEntity {
        return getCurrentCheckpoints(date, listOf(room))[room.id]
            ?: throw CustomException(AttendanceError.TIME_UNAVAILABLE)
    }

    fun getCurrentCheckpoints(
        date: LocalDate,
        rooms: List<RoomEntity>
    ): Map<Long, AttendanceCheckpointEntity?> {
        if (rooms.isEmpty()) return emptyMap()

        val dayOfWeek = date.dayOfWeek
        val generalCheckpoint = checkpointResolver.getCurrentCheckpointOrNull()
        val firstGradeCheckpoint = checkpointResolver.getCurrentCheckpointOrNull(FIRST_GRADE, dayOfWeek)
        val firstGradeRoomIds = if (firstGradeCheckpoint?.isFirstGradeSpecific() == true) {
            findFirstGradeRoomIds(date, firstGradeCheckpoint)
        } else {
            emptySet()
        }

        return rooms.mapNotNull { room ->
            val roomId = room.id ?: return@mapNotNull null
            val checkpoint = when (room.grade) {
                FIRST_GRADE -> firstGradeCheckpoint
                null -> if (roomId in firstGradeRoomIds) firstGradeCheckpoint else generalCheckpoint
                else -> generalCheckpoint
            }
            roomId to checkpoint
        }.toMap()
    }

    private fun findFirstGradeRoomIds(
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity
    ): Set<Long> {
        val scheduledRoomIds = attendanceQueryRepository.findScheduledRoomIdsByGradeAndCheckpoint(
            grade = FIRST_GRADE,
            dayOfWeek = date.dayOfWeek,
            checkpoint = checkpoint
        )
        val sortMode = attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
            date = date,
            checkpoint = checkpoint,
            grade = FIRST_GRADE
        ) ?: return scheduledRoomIds
        val activityRoomIds = attendanceQueryRepository.findActivityRoomIdsByGradeDayAndType(
            grade = FIRST_GRADE,
            dayOfWeek = date.dayOfWeek,
            type = sortMode.type
        )

        return scheduledRoomIds + activityRoomIds
    }

    private fun AttendanceCheckpointEntity.isFirstGradeSpecific(): Boolean {
        return grade == FIRST_GRADE && dayOfWeek != null
    }

    companion object {
        private const val FIRST_GRADE = 1
    }
}
