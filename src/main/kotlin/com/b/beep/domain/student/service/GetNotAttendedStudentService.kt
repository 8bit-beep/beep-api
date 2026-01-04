package com.b.beep.domain.student.service

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.student.controller.dto.response.StudentResponse
import com.b.beep.domain.student.repository.StudentQueryRepository
import com.b.beep.domain.user.domain.UserError
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.domain.room.fixedroom.repository.FixedRoomRepository
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetNotAttendedStudentService(
    private val userRepository: UserRepository,
    private val studentQueryRepository: StudentQueryRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val fixedRoomRepository: FixedRoomRepository,
    private val attendanceRepository: AttendanceRepository,
    private val roomRepository: RoomRepository,
) {
    fun getAll(type: AttendanceType): List<StudentResponse> {
        val users = userRepository.findAllByCurrentStatusAndRole(AttendanceType.NOT_ATTEND, UserRole.STUDENT)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val fixedRooms = fixedRoomRepository.findAllByUserAndType(user, type)
            val attendances = attendanceRepository.findByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, fixedRooms, attendances)
        }
    }

    fun getAllByRoomAndType(roomId: Long, type: AttendanceType): List<StudentResponse> {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw CustomException(RoomError.ROOM_NOT_FOUND)
        val users = studentQueryRepository.findAllByStatusAndRoomAndType(room, type, AttendanceType.NOT_ATTEND)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val fixedRooms = fixedRoomRepository.findAllByUserAndType(user, type)
            val attendances = attendanceRepository.findByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, fixedRooms, attendances)
        }
    }

    fun getAllByGradeAndCls(grade: Int, cls: Int): List<StudentResponse> {
        val users = studentQueryRepository.findAllByStatusAndGradeAndCls(grade, cls, AttendanceType.NOT_ATTEND)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val attendances = attendanceRepository.findByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, null, attendances)
        }
    }
}
