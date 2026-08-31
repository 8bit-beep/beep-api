package com.b.beep.domain.absence.controller.dto.response

data class OutSleepingResponse(
    val content: List<OutSleepingStudentResponse>,
)

data class OutSleepingStudentResponse(
    val publicId: String?,
    val name: String,
    val grade: Int,
    val room: Int,
    val number: Int,
)
