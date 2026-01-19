package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.service.AttendanceTypeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "출석 타입", description = "출석 타입 관리 API")
@Validated
@RestController
@RequestMapping("/types")
class AttendanceTypeController(
    private val attendanceTypeService: AttendanceTypeService
) {
    @Operation(summary = "출석 타입 생성", description = "새로운 출석 타입을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createType(@Valid @RequestBody request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        return attendanceTypeService.createType(request)
    }

    @Operation(summary = "출석 타입 목록 조회", description = "모든 출석 타입을 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getTypes(): List<AttendanceTypeResponse> {
        return attendanceTypeService.getAttendanceTypes()
    }

    @Operation(summary = "출석 타입 단건 조회", description = "ID로 출석 타입을 조회합니다.")
    @GetMapping("/{typeId}")
    @ResponseStatus(HttpStatus.OK)
    fun getType(@Parameter(description = "출석 타입 ID") @PathVariable @Positive(message = "ID는 양수여야 합니다") typeId: Long): AttendanceTypeResponse {
        return attendanceTypeService.getAttendanceType(typeId)
    }

    @Operation(summary = "출석 타입 수정", description = "출석 타입을 수정합니다.")
    @PatchMapping("/{typeId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateType(
        @Parameter(description = "출석 타입 ID") @PathVariable @Positive(message = "ID는 양수여야 합니다") typeId: Long,
        @Valid @RequestBody request: UpdateAttendanceTypeRequest
    ): AttendanceTypeResponse {
        return attendanceTypeService.updateType(typeId, request)
    }

    @Operation(summary = "출석 타입 삭제", description = "출석 타입을 삭제합니다.")
    @DeleteMapping("/{typeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteType(@Parameter(description = "출석 타입 ID") @PathVariable @Positive(message = "ID는 양수여야 합니다") typeId: Long) {
        attendanceTypeService.deleteType(typeId)
    }
}
