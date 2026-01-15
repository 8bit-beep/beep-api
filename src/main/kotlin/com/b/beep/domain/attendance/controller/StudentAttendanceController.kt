package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.AttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.AttendRequest
import com.b.beep.domain.attendance.service.StudentAttendanceService
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/attendances")
class StudentAttendanceController(
    private val studentAttendanceService: StudentAttendanceService
) : AttendanceDocs {
    @PostMapping
    override fun attend(@RequestBody request: AttendRequest) {
        studentAttendanceService.attend(request)
    }

    @PatchMapping("/cancel")
    override fun cancelAttendance() {
        studentAttendanceService.cancelAttendance()
    }
}
