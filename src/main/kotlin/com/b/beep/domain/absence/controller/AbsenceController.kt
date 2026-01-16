package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.docs.AbsenceDocs
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.service.AbsenceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/absences")
class AbsenceController(
    private val absenceService: AbsenceService,
) : AbsenceDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAbsence(@RequestBody request: CreateAbsenceRequest) {
        absenceService.createAbsence(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAbsences(): List<AbsenceResponse> {
        return absenceService.getAbsences()
    }

    @PatchMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateAbsence(@PathVariable absenceId: Long, @RequestBody request: UpdateAbsenceRequest) {
        absenceService.updateAbsence(absenceId, request)
    }

    @DeleteMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteAbsence(@PathVariable absenceId: Long) {
        absenceService.deleteAbsence(absenceId)
    }
}
