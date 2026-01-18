package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.docs.AttendanceTypeDocs
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
) : AttendanceTypeDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createType(@Valid @RequestBody request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        return attendanceTypeService.createType(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getTypes(): List<AttendanceTypeResponse> {
        return attendanceTypeService.getAttendanceTypes()
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun getType(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long): AttendanceTypeResponse {
        return attendanceTypeService.getAttendanceType(id)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateType(
        @PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long,
        @Valid @RequestBody request: UpdateAttendanceTypeRequest
    ): AttendanceTypeResponse {
        return attendanceTypeService.updateType(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteType(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long) {
        attendanceTypeService.deleteType(id)
    }
}
