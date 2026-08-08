package com.b.beep.domain.room.service

import com.b.beep.domain.attendance.domain.RoomCheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.domain.RoomClubNameResolver
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class RoomService(
    private val roomRepository: RoomRepository,
    private val roomCheckpointResolver: RoomCheckpointResolver,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val attendanceRepository: AttendanceRepository,
    private val roomClubNameResolver: RoomClubNameResolver
) {
    fun createRoom(request: CreateRoomRequest): RoomResponse {
        if (roomRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        val room = roomRepository.save(
            RoomEntity(
                name = request.name,
                grade = request.grade,
                classNumber = request.classNumber,
                floor = request.floor,
                clubName = request.clubName
            )
        )
        return RoomResponse.of(room)
    }

    @Transactional(readOnly = true)
    fun getRooms(): List<RoomResponse> {
        val rooms = roomRepository.findAllByIsDeletedFalse()
            .sortedWith(compareBy(
                { it.floor ?: Int.MAX_VALUE },
                { getRoomSortPriority(it) },
                { it.grade ?: Int.MAX_VALUE },
                { it.classNumber ?: Int.MAX_VALUE },
                { it.name }
            ))

        val today = getToday()
        val checkpointByRoomId = roomCheckpointResolver.getCurrentCheckpoints(today, rooms)
        val displayNameByRoomId = roomClubNameResolver.resolveDisplayNames(rooms, today)

        return rooms.map { room ->
            RoomResponse.of(
                room,
                name = displayNameByRoomId[room.id] ?: room.name,
                currentStudentCount = computeCurrentStudentCount(room, checkpointByRoomId[room.id], today)
            )
        }
    }

    private fun getRoomSortPriority(room: RoomEntity): Int {
        return when {
            room.grade != null && room.classNumber != null -> 0
            room.name.startsWith("프로젝트") -> 1
            room.name.contains("랩") -> 2
            else -> 3
        }
    }

    @Transactional(readOnly = true)
    fun getRoom(roomId: Long): RoomResponse {
        val room = getRoomEntityById(roomId)
        val today = getToday()
        val checkpoint = roomCheckpointResolver.getCurrentCheckpoints(today, listOf(room))[room.id]
        val displayName = roomClubNameResolver.resolveDisplayNames(listOf(room), today)[room.id] ?: room.name
        return RoomResponse.of(
            room,
            name = displayName,
            currentStudentCount = computeCurrentStudentCount(room, checkpoint, today)
        )
    }

    fun updateRoom(roomId: Long, request: UpdateRoomRequest): RoomResponse {
        val room = getRoomEntityById(roomId)
        if (room.name != request.name && roomRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(RoomError.ROOM_ALREADY_EXISTS)
        }
        room.name = request.name
        room.grade = request.grade
        room.classNumber = request.classNumber
        room.floor = request.floor
        room.clubName = request.clubName
        return RoomResponse.of(room)
    }

    fun deleteRoom(roomId: Long) {
        val room = getRoomEntityById(roomId)
        room.name = "${room.name}_deleted_${room.id}"
        room.isDeleted = true
    }

    fun getRoomEntityById(roomId: Long): RoomEntity {
        return roomRepository.findByIdAndIsDeletedFalse(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
    }

    private fun getToday(): LocalDate {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
    }

    // 현재 반 안에 있는 학생 수 = 이 실에 스케줄된 학생 중 현재 체크포인트 출석 타입이 "교실자습"인 학생 수
    // 미출석(출석 기록 없음)을 포함해 교실자습이 아닌 학생은 전부 제외한다.
    private fun computeCurrentStudentCount(
        room: RoomEntity,
        checkpoint: AttendanceCheckpointEntity?,
        today: LocalDate
    ): Int {
        if (checkpoint == null) return 0

        val schedules = studentScheduleRepository.findAllByRoomAndDayOfWeekAndCheckpoint(
            room, today.dayOfWeek, checkpoint
        )
        if (schedules.isEmpty()) return 0

        val users = schedules.map { it.user }
        val typeNameByUserId = attendanceRepository
            .findAllByUsersAndCheckpointIdAndDate(users, checkpoint.id!!, today)
            .associate { it.user.id to it.type.name }

        return users.count { user ->
            typeNameByUserId[user.id] == AttendanceTypeEntity.CLASSROOM_STUDY_TYPE_NAME
        }
    }
}
