package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse

data class AbsenceStudentResponse(
    val username: String,
    val name: String,
    val info: StudentInfoResponse? = null,
)
