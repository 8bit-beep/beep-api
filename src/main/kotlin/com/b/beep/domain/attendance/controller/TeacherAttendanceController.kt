package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.TeacherAttendanceDocs
import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.service.TeacherAttendanceService
import com.b.beep.global.common.dto.PageResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
        teacherAttendanceService.updateStudentStatus(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAttendances(
        @RequestParam(required = false) @Positive(message = "실 ID는 양수여야 합니다") roomId: Long?,
        @RequestParam(required = false) @Positive(message = "상태 ID는 양수여야 합니다") statusId: Long?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?,
        @RequestParam(required = false, defaultValue = "true") isCurrentCheckpoint: Boolean,
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable
    ): PageResponse<AttendanceStudentResponse> {
        return PageResponse.from(
            teacherAttendanceService.getAttendances(
                roomId,
                statusId,
                grade,
                classNumber,
                isCurrentCheckpoint,
                pageable
            )
        )
    }
}
