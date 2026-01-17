package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.StudentAttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.service.StudentAttendanceService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/attendances")
class StudentAttendanceController(
    private val studentAttendanceService: StudentAttendanceService
) : StudentAttendanceDocs {
    @PostMapping
    override fun attend(@Valid @RequestBody request: CreateAttendanceRequest) {
        studentAttendanceService.attend(request)
    }

    @PatchMapping("/cancel")
    override fun cancelAttendance() {
        studentAttendanceService.cancelAttendance()
    }
}
