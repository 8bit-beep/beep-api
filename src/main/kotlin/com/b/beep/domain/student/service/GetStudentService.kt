package com.b.beep.domain.student.service

import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.student.controller.dto.response.StudentResponse
import com.b.beep.domain.student.repository.StudentQueryRepository
import com.b.beep.domain.user.domain.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class GetStudentService(
    private val userRepository: UserRepository,
    private val studentQueryRepository: StudentQueryRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository
) {
    fun getAllByRoomAndType(room: Room, type: AttendanceType): List<StudentResponse> {
        val users = studentQueryRepository.findAllByStatusAndRoomAndType(room, type)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val attendances = attendanceRepository.findByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, attendances)
        }
    }

    fun getAllByGradeAndCls(grade: Int, cls: Int): List<StudentResponse> {
        val users = studentQueryRepository.findAllByStatusAndGradeAndCls(grade, cls)
        return users.map { user ->
            val studentInfo = studentInfoRepository.findByUser(user)
                ?: throw CustomException(UserError.STUDENT_INFO_NOT_FOUND)
            val attendances = attendanceRepository.findByUserAndDate(user, LocalDate.now())
            StudentResponse.of(user, studentInfo, attendances)
        }
    }
}
