package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.service.AttendanceTypeService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/types")
class AttendanceTypeController(
    private val attendanceTypeService: AttendanceTypeService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createType(@Valid @RequestBody request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        return attendanceTypeService.createType(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getTypes(): List<AttendanceTypeResponse> {
        return attendanceTypeService.getTypes()
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getType(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long): AttendanceTypeResponse {
        return attendanceTypeService.getType(id)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateType(
        @PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long,
        @Valid @RequestBody request: UpdateAttendanceTypeRequest
    ): AttendanceTypeResponse {
        return attendanceTypeService.updateType(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteType(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long) {
        attendanceTypeService.deleteType(id)
    }
}
