package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.docs.AbsenceDocs
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.CreateAbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.UpdateAbsenceResponse
import com.b.beep.domain.absence.service.AbsenceService
import com.b.beep.global.common.dto.PageResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/absences")
class AbsenceController(
    private val absenceService: AbsenceService,
) : AbsenceDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAbsence(@Valid @RequestBody request: CreateAbsenceRequest): CreateAbsenceResponse {
        return absenceService.createAbsence(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAbsences(
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable
    ): PageResponse<AbsenceResponse> {
        return PageResponse.from(absenceService.getAbsences(pageable))
    }

    @PatchMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateAbsence(
        @PathVariable @Positive(message = "결석 ID는 양수여야 합니다") absenceId: Long,
        @Valid @RequestBody request: UpdateAbsenceRequest
    ): UpdateAbsenceResponse {
        return absenceService.updateAbsence(absenceId, request)
    }

    @DeleteMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteAbsence(
        @PathVariable @Positive(message = "부재 ID는 양수여야 합니다") absenceId: Long
    ) {
        absenceService.deleteAbsence(absenceId)
    }
}
