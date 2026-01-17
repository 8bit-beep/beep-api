package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.TeacherAttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.service.TeacherAttendanceService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/attendances")
class TeacherAttendanceController(
    private val teacherAttendanceService: TeacherAttendanceService
) : TeacherAttendanceDocs {
    @PatchMapping("/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun updateStatus(@Valid @RequestBody request: UpdateStatusRequest) {
        teacherAttendanceService.updateStudentStatus(
            grade = request.grade,
            classNumber = request.classNumber,
            num = request.num,
            statusId = request.statusId,
            date = request.date,
            checkpointId = request.checkpointId
        )
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAll(
        @RequestParam(required = false) @Positive(message = "실 ID는 양수여야 합니다") roomId: Long?,
        @RequestParam(required = false) @Positive(message = "상태 ID는 양수여야 합니다") statusId: Long?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?,
        @RequestParam(required = false) scheduleOnly: Boolean?
    ): List<AttendanceStudentResponse> {
        return teacherAttendanceService.findAll(roomId, statusId, grade, classNumber, scheduleOnly)
    }
}
