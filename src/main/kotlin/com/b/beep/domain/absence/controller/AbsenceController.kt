package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.docs.AbsenceDocs
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.service.AbsenceService
import com.b.beep.domain.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/long-absences")
class AbsenceController(
    private val absenceService: AbsenceService,
    private val userService: UserService
) : AbsenceDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAbsence(@RequestBody request: CreateAbsenceRequest) {
        absenceService.create(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAllAbsences(): List<AbsenceResponse> {
        return absenceService.getAll().map {
            AbsenceResponse.of(it, userService.getStudentInfo(it.user))
        }
    }

    @PatchMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateAbsence(@PathVariable("absenceId") id: Long, @RequestBody request: UpdateAbsenceRequest) {
        absenceService.update(id, request)
    }

    @DeleteMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteAbsence(@PathVariable("absenceId") id: Long) {
        absenceService.delete(id)
    }
}
