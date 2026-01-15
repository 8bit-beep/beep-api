package com.b.beep.domain.absence.controller.dto.response

import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.domain.enums.entity.StudentInfoEntity
import java.time.LocalDate

data class AbsenceResponse(
    val absenceId: Long,
    val studentName: String,
    var studentInfo: StudentInfoResponse? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String
) {
    companion object {
        fun of(absence: AbsenceEntity, studentInfo: StudentInfoEntity? = null): AbsenceResponse {
            return AbsenceResponse(
                absenceId = absence.id!!,
                studentName = absence.user.username,
                studentInfo = studentInfo?.let { StudentInfoResponse.of(it) },
                startDate = absence.startDate,
                endDate = absence.endDate,
                reason = absence.reason
            )
        }
    }
}
