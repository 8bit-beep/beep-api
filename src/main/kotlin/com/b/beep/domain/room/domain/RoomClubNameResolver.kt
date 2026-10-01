package com.b.beep.domain.room.domain

import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceSortModeDefaultRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RoomClubNameResolver(
    private val checkpointResolver: CheckpointResolver,
    private val attendanceSortModeDefaultRepository: AttendanceSortModeDefaultRepository,
    private val attendanceSortModeRepository: AttendanceSortModeRepository,
    private val attendanceTypeService: AttendanceTypeService
) {
    fun resolveDisplayNames(rooms: List<RoomEntity>, date: LocalDate): Map<Long, String> {
        val clubRooms = rooms.filter { it.id != null && !it.clubName.isNullOrBlank() }
        if (clubRooms.isEmpty()) {
            return rooms.mapNotNull { room -> room.id?.let { it to room.name } }.toMap()
        }

        val clubType = attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME)
        val checkpointByGrade = checkpointResolver.getCurrentCheckpointsOrNearest(GRADES, date.dayOfWeek)
        val checkpoints = checkpointByGrade.values.distinctBy { it.id }
        val modesByGradeAndCheckpoint = attendanceSortModeRepository
            .findAllByDateAndCheckpointIn(date, checkpoints)
            .associateBy { it.grade to it.checkpoint.id }
        val defaultsByGradeAndCheckpoint = attendanceSortModeDefaultRepository
            .findAllByDayOfWeekAndCheckpointInAndTypeIsDeletedFalse(date.dayOfWeek, checkpoints)
            .associateBy { it.grade to it.checkpoint.id }
        val clubGrades = GRADES.filter { grade ->
            val checkpoint = checkpointByGrade.getValue(grade)
            val sortModeType = modesByGradeAndCheckpoint[grade to checkpoint.id]?.type
            val defaultType = defaultsByGradeAndCheckpoint[grade to checkpoint.id]?.type
                ?.takeIf { it.name in AttendanceTypeEntity.SORT_MODE_TYPE_NAMES }
            // 날짜별 변경값이 있으면 요일별 기본 스케줄보다 우선합니다.
            val effectiveType = sortModeType ?: defaultType
            effectiveType?.id == clubType.id
        }.toSet()

        return rooms.mapNotNull { room ->
            val roomId = room.id ?: return@mapNotNull null
            val isClubRoom = !room.clubName.isNullOrBlank() &&
                (room.grade?.let { it in clubGrades } ?: clubGrades.isNotEmpty())
            roomId to if (isClubRoom) room.clubName!! else room.name
        }.toMap()
    }

    companion object {
        private val GRADES = listOf(1, 2, 3)
    }
}
