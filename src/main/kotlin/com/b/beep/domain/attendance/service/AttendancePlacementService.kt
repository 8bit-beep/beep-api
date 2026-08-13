package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class AttendancePlacementService(
    private val attendanceRepository: AttendanceRepository,
    private val attendanceSortModeRepository: AttendanceSortModeRepository,
    private val studentActivityRoomRepository: StudentActivityRoomRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val roomRepository: RoomRepository
) {
    fun resolveRooms(
        users: List<UserEntity>,
        date: LocalDate,
        checkpoint: AttendanceCheckpointEntity
    ): Map<Long, RoomEntity> {
        if (users.isEmpty()) return emptyMap()

        val dayOfWeek = date.dayOfWeek
        val attendanceRoomByUserId = attendanceRepository
            .findAllByUsersAndCheckpointIdAndDate(users, checkpoint.id!!, date)
            .mapNotNull { attendance ->
                val userId = attendance.user.id ?: return@mapNotNull null
                val room = attendance.room ?: return@mapNotNull null
                userId to room
            }
            .toMap()

        val studentInfoByUserId = studentInfoRepository.findAllByUserIn(users)
            .mapNotNull { studentInfo ->
                val userId = studentInfo.user.id ?: return@mapNotNull null
                userId to studentInfo
            }
            .toMap()
        val sortModeByGrade = attendanceSortModeRepository.findAllByDateAndCheckpoint(date, checkpoint)
            .associateBy { it.grade }
        val activityRoomByUserTypeAndDay = studentActivityRoomRepository
            .findAllByUserInAndDayOfWeekOrCommon(users, dayOfWeek)
            .mapNotNull { activityRoom ->
                val userId = activityRoom.user.id ?: return@mapNotNull null
                val typeId = activityRoom.type.id ?: return@mapNotNull null
                Triple(userId, typeId, activityRoom.dayOfWeek) to activityRoom.room
            }
            .toMap()
        val homeroomByGradeAndClass = if (sortModeByGrade.values.any {
                it.type.name == AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME
            }) {
            roomRepository.findAllByGradeIsNotNullAndClassNumberIsNotNullAndIsDeletedFalse()
                .associateBy { it.grade!! to it.classNumber!! }
        } else {
            emptyMap()
        }
        val scheduleByUserId = studentScheduleRepository.findAllByUserInAndDayOfWeekIn(users, listOf(dayOfWeek))
            .filter { it.checkpoint.overlaps(checkpoint) }
            .groupBy { it.user.id }

        return users.mapNotNull { user ->
            val userId = user.id ?: return@mapNotNull null
            val attendanceRoom = attendanceRoomByUserId[userId]
            if (attendanceRoom != null) return@mapNotNull userId to attendanceRoom

            val fallbackRoom = scheduleByUserId[userId].resolveFallbackRoom(checkpoint)
            val studentInfo = studentInfoByUserId[userId]
            val sortModeType = studentInfo?.grade?.let { sortModeByGrade[it]?.type }
            val sortModeRoom = when {
                sortModeType == null -> null
                sortModeType.name == AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME -> {
                    homeroomByGradeAndClass[studentInfo.grade to studentInfo.classNumber]
                }
                sortModeType.name in AttendanceTypeEntity.ACTIVITY_ROOM_TYPE_NAMES -> {
                    val typeId = sortModeType.id
                    if (typeId == null) {
                        null
                    } else {
                        activityRoomByUserTypeAndDay[Triple(userId, typeId, dayOfWeek)]
                            ?: activityRoomByUserTypeAndDay[Triple(userId, typeId, null)]
                    }
                }
                else -> null
            }

            val room = sortModeRoom ?: fallbackRoom ?: return@mapNotNull null
            userId to room
        }.toMap()
    }

    private fun List<StudentScheduleEntity>?.resolveFallbackRoom(
        checkpoint: AttendanceCheckpointEntity
    ): RoomEntity? {
        if (isNullOrEmpty()) return null

        return firstOrNull { it.checkpoint.id == checkpoint.id }?.room
            ?: sortedBy { it.checkpoint.startAt }.firstOrNull()?.room
    }

    private fun AttendanceCheckpointEntity.overlaps(other: AttendanceCheckpointEntity): Boolean {
        return startAt < other.endAt && other.startAt < endAt
    }
}
