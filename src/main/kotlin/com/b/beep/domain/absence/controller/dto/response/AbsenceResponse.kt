package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import java.time.LocalDate

data class AbsenceResponse(
    val absenceId: Long,
    val studentName: String,
    val studentInfo: StudentInfoResponse? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String
)
