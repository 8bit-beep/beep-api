package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.controller.dto.request.StudentActivityRoomRequest
import com.b.beep.domain.user.controller.dto.response.StudentActivityRoomResponse
import com.b.beep.domain.user.domain.entity.StudentActivityRoomEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentActivityRoomRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        val entities = requests.map { request ->
            val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)
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
            .sortedWith(compareBy({ it.dayOfWeek.value }, { it.type.id ?: Long.MAX_VALUE }))
            .map { StudentActivityRoomResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getActivityRooms(studentId: Long): List<StudentActivityRoomResponse> {
        val user = getUserEntity(studentId)
        return studentActivityRoomRepository.findAllByUser(user)
            .sortedWith(compareBy({ it.dayOfWeek.value }, { it.type.id ?: Long.MAX_VALUE }))
            .map { StudentActivityRoomResponse.of(it) }
    }

    private fun getUserEntity(studentId: Long): UserEntity {
        return userRepository.findByIdAndIsDeletedFalse(studentId)
            ?: throw CustomException(UserError.USER_NOT_FOUND)
    }
}
