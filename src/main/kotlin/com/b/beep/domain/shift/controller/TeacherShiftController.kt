package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.TeacherShiftDocs
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.service.TeacherShiftService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shifts")
class TeacherShiftController(
    private val teacherShiftService: TeacherShiftService,
) : TeacherShiftDocs {
    @GetMapping
    override fun getShifts(): List<ShiftResponse> {
        return teacherShiftService.getAll()
    }

    @PatchMapping("/{shiftId}/status")
    override fun updateShiftStatus(
        @PathVariable shiftId: Long,
        @RequestParam status: ShiftStatus
    ) {
        teacherShiftService.updateStatus(shiftId, status)
    }
}