package com.b.beep.domain.user.service

import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.repository.StudentQueryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StudentService(
    private val studentQueryRepository: StudentQueryRepository
) {
    fun findStudents(
        grade: Int?,
        classNumber: Int?,
        keyword: String?
    ): List<StudentResponse> {
        return studentQueryRepository
            .findAllByFilters(grade, classNumber, keyword)
            .map { StudentResponse.of(it) }
    }
}

