package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.response.OutSleepingResponse
import com.b.beep.domain.absence.controller.dto.response.OutSleepingStudentResponse
import com.b.beep.domain.absence.repository.OutSleepingQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class OutSleepingOpenApiService(
    private val outSleepingQueryRepository: OutSleepingQueryRepository,
) {
    fun search(date: LocalDate): OutSleepingResponse {
        val students = outSleepingQueryRepository.findAllStudents(date).map {
            OutSleepingStudentResponse(
                publicId = it.publicId,
                name = it.name,
                grade = it.grade,
                room = it.room,
                number = it.number,
            )
        }
        return OutSleepingResponse(content = students)
    }
}
