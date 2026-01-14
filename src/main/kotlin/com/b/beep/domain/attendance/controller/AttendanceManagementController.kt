package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.AttendanceManagementDocs
import com.b.beep.domain.attendance.controller.dto.request.PreAttendRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.service.AttendanceManagementService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/attendance")
class AttendanceManagementController(
    private val attendanceManagementService: AttendanceManagementService
) : AttendanceManagementDocs {
    @PatchMapping("/pre-attend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun preAttend(@RequestBody request: PreAttendRequest) {
        attendanceManagementService.createPresetAttendance(
            grade = request.grade,
            classNumber = request.classNumber,
            num = request.num,
            status = request.status,
            period = request.period
        )
    }

    @PatchMapping("/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun updateStatus(@RequestBody request: UpdateStatusRequest) {
        attendanceManagementService.updateCurrentStudentStatus(
            grade = request.grade,
            classNumber = request.classNumber,
            num = request.num,
            status = request.status
        )
    }
}
