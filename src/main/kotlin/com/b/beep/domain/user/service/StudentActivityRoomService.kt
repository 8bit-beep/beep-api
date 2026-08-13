package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.StudentActivityRoomRequest
import com.b.beep.domain.user.controller.dto.response.StudentActivityRoomResponse
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.StudentActivityRoomError
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek

@Service
@Transactional
class StudentActivityRoomService(
    private val studentActivityRoomRepository: StudentActivityRoomRepository,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val attendanceTypeService: AttendanceTypeService
) {
    fun replaceActivityRooms(
        studentId: Long,
        requests: List<StudentActivityRoomRequest>
    ): List<StudentActivityRoomResponse> {
        val user = getUserEntity(studentId)
        validateDuplicates(requests)
        val entities = requests.map { request ->
            val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)
            validateActivityRoom(type, request.dayOfWeek)
            val room = roomRepository.findByIdAndIsDeletedFalse(request.roomId)
                ?: throw CustomException(RoomError.ROOM_NOT_FOUND)

            StudentActivityRoomEntity(
                user = user,
                dayOfWeek = request.dayOfWeek,
                type = type,
                room = room
            )
        }

        studentActivityRoomRepository.deleteAllByUser(user)
        studentActivityRoomRepository.flush()

        return studentActivityRoomRepository.saveAll(entities)
            .sortedWith(activityRoomComparator)
            .map { StudentActivityRoomResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getActivityRooms(studentId: Long): List<StudentActivityRoomResponse> {
        val user = getUserEntity(studentId)
        return studentActivityRoomRepository.findAllByUser(user)
            .sortedWith(activityRoomComparator)
            .map { StudentActivityRoomResponse.of(it) }
    }

    private fun validateDuplicates(requests: List<StudentActivityRoomRequest>) {
        if (requests.groupingBy { it.typeId to it.dayOfWeek }.eachCount().any { it.value > 1 }) {
            throw CustomException(StudentActivityRoomError.DUPLICATE_ASSIGNMENT)
        }
    }

    private fun validateActivityRoom(type: AttendanceTypeEntity, dayOfWeek: DayOfWeek?) {
        if (type.name !in AttendanceTypeEntity.ACTIVITY_ROOM_TYPE_NAMES) {
            throw CustomException(StudentActivityRoomError.UNSUPPORTED_TYPE)
        }
        if (type.name in AttendanceTypeEntity.COMMON_ACTIVITY_ROOM_TYPE_NAMES && dayOfWeek != null) {
            throw CustomException(StudentActivityRoomError.DAY_OF_WEEK_NOT_ALLOWED)
        }
        if (type.name == AttendanceTypeEntity.AFTER_SCHOOL_TYPE_NAME) {
            if (dayOfWeek == null) {
                throw CustomException(StudentActivityRoomError.DAY_OF_WEEK_REQUIRED)
            }
            if (dayOfWeek !in ACTIVITY_DAYS) {
                throw CustomException(StudentActivityRoomError.INVALID_DAY_OF_WEEK)
            }
        }
    }

    private fun getUserEntity(studentId: Long): UserEntity {
        return userRepository.findByIdAndIsDeletedFalse(studentId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
    }

    companion object {
        private val ACTIVITY_DAYS = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY
        )
        private val activityRoomComparator = compareBy<StudentActivityRoomEntity>(
            { it.dayOfWeek?.value ?: 0 },
            { it.type.id ?: Long.MAX_VALUE }
        )
    }
}
