package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.StudentShiftDocs
import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.service.StudentShiftService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/shifts")
class StudentShiftController(
    private val studentShiftService: StudentShiftService,
) : StudentShiftDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createShift(@Valid @RequestBody request: CreateShiftRequest) {
        studentShiftService.createShift(request)
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    override fun getMyShifts(): List<ShiftResponse> {
        return studentShiftService.getMyShifts()
    }

    @PatchMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateShift(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long,
        @Valid @RequestBody request: UpdateShiftRequest
    ) {
        studentShiftService.updateShift(shiftId, request)
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteShift(
        @PathVariable @Positive(message = "이석 ID는 양수여야 합니다") shiftId: Long
    ) {
        studentShiftService.deleteShift(shiftId)
    }
}
