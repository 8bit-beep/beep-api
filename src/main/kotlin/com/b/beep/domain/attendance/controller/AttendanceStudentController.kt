package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.AttendanceStudentDocs
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.domain.enums.Room
import com.b.beep.domain.attendance.service.AttendanceStudentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/attendance/students")
class AttendanceStudentController(
    private val attendanceStudentService: AttendanceStudentService
) : AttendanceStudentDocs {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAll(
        @RequestParam(required = false) room: Room?,
        @RequestParam(required = false) type: AttendanceType?,
        @RequestParam(required = false) status: AttendanceType?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?
    ): List<AttendanceStudentResponse> {
        return attendanceStudentService.findAll(room, type, status, grade, classNumber)
    }
}
