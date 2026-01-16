package com.b.beep.domain.absence.controller.docs

import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "장기결석", description = "장기결석 API")
interface AbsenceDocs {
    @Operation(summary = "장기결석 생성")
    fun createAbsence(@RequestBody request: CreateAbsenceRequest)

    @Operation(summary = "모든 장기결석 조회")
    fun getAbsences(): List<AbsenceResponse>

    @Operation(summary = "장기결석 수정")
    fun updateAbsence(@PathVariable absenceId: Long, @RequestBody request: UpdateAbsenceRequest)

    @Operation(summary = "장기결석 삭제")
    fun deleteAbsence(@PathVariable absenceId: Long)
}
