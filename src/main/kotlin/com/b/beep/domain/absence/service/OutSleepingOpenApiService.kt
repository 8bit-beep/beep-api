package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.response.OutSleepingResponse
import com.b.beep.domain.absence.controller.dto.response.OutSleepingContentResponse
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
        val content = outSleepingQueryRepository.findAll(date).map {
            OutSleepingContentResponse(
                publicId = it.publicId,
                reason = it.reason,
                student = OutSleepingStudentResponse(
                    name = it.studentName,
                    grade = it.grade,
                    room = it.room,
                    number = it.number,
                ),
                startAt = it.startAt,
                endAt = it.endAt,
            )
        }
        return OutSleepingResponse(content = content)
    }
}
