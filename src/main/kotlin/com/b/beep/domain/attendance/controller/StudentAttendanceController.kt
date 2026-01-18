package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.StudentAttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.service.StudentAttendanceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/attendances")
class StudentAttendanceController(
    private val studentAttendanceService: StudentAttendanceService
) : StudentAttendanceDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun attend(@Valid @RequestBody request: CreateAttendanceRequest) {
        studentAttendanceService.attend(request)
    }

    @PatchMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun cancelAttendance() {
        studentAttendanceService.cancelAttendance()
    }
}
