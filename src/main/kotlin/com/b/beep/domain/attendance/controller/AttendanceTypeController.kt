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
    private val typeService: AttendanceTypeService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        return typeService.create(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun findAll(): List<AttendanceTypeResponse> {
        return typeService.findAll()
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun findById(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long): AttendanceTypeResponse {
        return typeService.findById(id)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long,
        @Valid @RequestBody request: UpdateAttendanceTypeRequest
    ): AttendanceTypeResponse {
        return typeService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable @Positive(message = "ID는 양수여야 합니다") id: Long) {
        typeService.delete(id)
    }
}
