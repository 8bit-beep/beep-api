package com.b.beep.domain.room.domain

import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RoomClubNameResolver(
    private val roomCheckpointResolver: RoomCheckpointResolver,
    private val attendanceQueryRepository: AttendanceQueryRepository,
    private val attendanceSortModeRepository: AttendanceSortModeRepository,
    private val attendanceTypeService: AttendanceTypeService
) {
    fun resolveDisplayNames(rooms: List<RoomEntity>, date: LocalDate): Map<Long, String> {
        val clubRooms = rooms.filter { it.id != null && !it.clubName.isNullOrBlank() }
        if (clubRooms.isEmpty()) {
            return rooms.mapNotNull { room -> room.id?.let { it to room.name } }.toMap()
        }

        val clubType = attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.CLUB_TYPE_NAME)
        val checkpointByRoomId = clubRooms.associate { room ->
            room.id!! to roomCheckpointResolver.getCurrentCheckpointOrNearest(date, room, room.grade)
        }
        val clubRoomIdsByCheckpointId = checkpointByRoomId.values
            .distinctBy { it.id }
            .associate { checkpoint -> checkpoint.id to findClubRoomIds(date, checkpoint, clubType) }

        return rooms.mapNotNull { room ->
            val roomId = room.id ?: return@mapNotNull null
            val checkpoint = checkpointByRoomId[roomId]
            val isClubRoom = checkpoint != null && roomId in (clubRoomIdsByCheckpointId[checkpoint.id] ?: emptySet())
            roomId to if (isClubRoom) room.clubName!! else room.name
        }.toMap()
    }

    private fun findClubRoomIds(
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity,
        clubType: AttendanceTypeEntity
    ): Set<Long> {
        val dayOfWeek = date.dayOfWeek
        val scheduledClubRoomIds = attendanceQueryRepository
            .findClubMajorityRoomIdsByDayCheckpoint(dayOfWeek, checkpoint, clubType)

        val forcedClubRoomIds = GRADES
            .filter { grade ->
                attendanceSortModeRepository.findByDateAndCheckpointAndGrade(date, checkpoint, grade)
                    ?.type?.id == clubType.id
            }
            .flatMap { grade ->
                attendanceQueryRepository.findActivityRoomIdsByGradeDayOrCommonAndType(grade, dayOfWeek, clubType)
            }

        return scheduledClubRoomIds + forcedClubRoomIds
    }

    companion object {
        private val GRADES = listOf(1, 2, 3)
    }
}
