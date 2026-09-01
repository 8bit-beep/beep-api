package com.b.beep.domain.absence.controller.dto.response

import java.time.LocalDate

data class OutSleepingResponse(
    val content: List<OutSleepingContentResponse>,
)

data class OutSleepingContentResponse(
    val publicId: String?,
    val reason: String,
    val student: OutSleepingStudentResponse,
    val startAt: LocalDate,
    val endAt: LocalDate,
)

data class OutSleepingStudentResponse(
    val name: String,
    val grade: Int,
    val room: Int,
    val number: Int,
)
