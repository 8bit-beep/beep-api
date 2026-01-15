package com.b.beep.domain.shift.controller

import com.b.beep.domain.shift.controller.docs.ShiftManagementDocs
import com.b.beep.domain.shift.controller.dto.response.ShiftResponse
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.service.ShiftManagementService
import com.b.beep.domain.user.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shifts")
class ShiftManagementController(
    private val shiftManagementService: ShiftManagementService,
    private val userService: UserService
) : ShiftManagementDocs {
    @GetMapping
    override fun getShifts(): List<ShiftResponse> {
        return shiftManagementService.getAll().map {
            val studentInfo = userService.getStudentInfo(it.user)
            ShiftResponse.of(it, studentInfo)
        }
    }

    @PatchMapping("/{shiftId}/status")
    override fun updateShiftStatus(
        @PathVariable("shiftId") shiftId: Long,
        @RequestParam status: ShiftStatus
    ) {
        shiftManagementService.updateStatus(shiftId, status)
    }
}