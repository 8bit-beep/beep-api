package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.StudentShiftDocs
import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.service.StudentShiftService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shifts")
class StudentShiftController(
    private val studentShiftService: StudentShiftService,
) : StudentShiftDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createShift(@RequestBody request: CreateShiftRequest) {
        studentShiftService.createShift(request)
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    override fun getMyShifts(): List<ShiftResponse> {
        return studentShiftService.getMyShifts()
    }

    @PatchMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateShift(@PathVariable shiftId: Long, @RequestBody request: UpdateShiftRequest) {
        studentShiftService.updateShift(shiftId, request)
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteShift(@PathVariable shiftId: Long) {
        studentShiftService.deleteShift(shiftId)
    }
}