package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.TeacherAttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.PreAttendRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.service.TeacherAttendanceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/attendances")
class TeacherAttendanceController(
    private val teacherAttendanceService: TeacherAttendanceService
) : TeacherAttendanceDocs {
    @PatchMapping("/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun updateStatus(@RequestBody request: UpdateStatusRequest) {
        teacherAttendanceService.updateStudentStatus(
            grade = request.grade,
            classNumber = request.classNumber,
            num = request.num,
            status = request.status,
            date = request.date,
            period = request.period
        )
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAll(
        @RequestParam(required = false) roomId: Long?,
        @RequestParam(required = false) type: AttendanceType?,
        @RequestParam(required = false) status: AttendanceType?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?
    ): List<AttendanceStudentResponse> {
        return teacherAttendanceService.findAll(roomId, type, status, grade, classNumber)
    }
}
