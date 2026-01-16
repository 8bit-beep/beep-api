package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.ShiftDocs
import com.b.beep.domain.shift.controller.dto.request.CreateShiftRequest
import com.b.beep.domain.shift.controller.dto.request.UpdateShiftRequest
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.service.ShiftService
import com.b.beep.domain.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shifts")
class ShiftController(
    private val shiftService: ShiftService,
    private val userService: UserService
) : ShiftDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createShift(@RequestBody request: CreateShiftRequest) {
        shiftService.createShift(request)
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    override fun getMyShifts(): List<ShiftResponse> {
        val studentInfo = userService.getMyStudentInfo()
        return shiftService.getMyShifts().map { ShiftResponse.of(it, studentInfo) }
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    override fun updateShift(@RequestBody request: UpdateShiftRequest) {
        shiftService.updateShift(request.shiftId, request)
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteShift(@PathVariable("shiftId") id: Long) {
        shiftService.deleteShift(id)
    }
}