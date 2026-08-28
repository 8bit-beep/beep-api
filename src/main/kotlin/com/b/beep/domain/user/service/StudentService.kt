package com.b.beep.domain.user.service

import com.b.beep.domain.attendance.repository.AttendanceQueryRepository
import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.repository.StudentQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StudentService(
    private val studentQueryRepository: StudentQueryRepository,
    private val attendanceQueryRepository: AttendanceQueryRepository
) {
    fun findStudents(
        grade: Int?,
        classNumber: Int?,
        keyword: String?
    ): List<StudentResponse> {
        val students = studentQueryRepository.findAllByFilters(grade, classNumber, keyword)
        val usersByGrade = students
            .groupBy { it.grade }
            .mapValues { (_, studentInfos) -> studentInfos.map { it.user } }
        val currentTypeIds = attendanceQueryRepository.findCurrentTypeIds(usersByGrade)

        return students.map { student ->
            StudentResponse.of(student, currentTypeIds[student.user.id])
        }
    }
}
