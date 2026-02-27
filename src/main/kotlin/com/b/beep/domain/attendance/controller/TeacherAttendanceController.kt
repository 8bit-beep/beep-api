package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.UpdateStatusRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceStudentResponse
import com.b.beep.domain.attendance.service.TeacherAttendanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "출석 관리", description = "출석 관리 API")
@Validated
@RestController
@RequestMapping("/attendances")
class TeacherAttendanceController(
    private val teacherAttendanceService: TeacherAttendanceService
) {
    @Operation(
        summary = "출석 상태 변경",
        description = "학생 출석 상태를 변경합니다. date와 checkpointId 미입력시 오늘, 현재 체크포인트 기반으로 수정합니다. statudId란에는 type을 입력해주세요."
    )
    @PatchMapping("/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateStatus(@Valid @RequestBody request: UpdateStatusRequest) {
        teacherAttendanceService.updateStudentStatus(request)
    }

    @Operation(summary = "학생 조회", description = "조건에 맞는 학생 목록을 조회합니다. roomId 입력 시 해당 실 스케줄 학생만 조회됩니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAttendances(
        @Parameter(description = "조회 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @Parameter(description = "체크포인트 ID") @RequestParam(required = false) @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long?,
        @Parameter(description = "실 ID (입력 시 해당 실 스케줄 학생만 조회)") @RequestParam(required = false) @Positive(message = "실 ID는 양수여야 합니다") roomId: Long?,
        @Parameter(description = "출석 상태 ID(type ID) 필터") @RequestParam(required = false) @Positive(message = "상태 ID는 양수여야 합니다") statusId: Long?,
        @Parameter(description = "학년") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반") @RequestParam(required = false) classNumber: Int?,
        @Parameter(description = "현재 체크포인트 기준 필터 (기본값: true)") @RequestParam(required = false, defaultValue = "true") isCurrentCheckpoint: Boolean
    ): List<AttendanceStudentResponse> {
        return teacherAttendanceService.getAttendances(
            date = date,
            checkpointId = checkpointId,
            roomId,
            statusId,
            grade,
            classNumber,
            isCurrentCheckpoint
        )
    }

    @Operation(summary = "모든 체크포인트 출석 조회", description = "모든 교시의 출석 상태를 한 번에 조회합니다.")
    @GetMapping("/all-checkpoints")
    @ResponseStatus(HttpStatus.OK)
    fun getAllCheckpointAttendances(
        @Parameter(description = "조회 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @Parameter(description = "실 ID (입력 시 해당 실 스케줄 학생만 조회)") @RequestParam(required = false) @Positive(message = "실 ID는 양수여야 합니다") roomId: Long?,
        @Parameter(description = "출석 상태 ID(type ID) 필터") @RequestParam(required = false) @Positive(message = "상태 ID는 양수여야 합니다") statusId: Long?,
        @Parameter(description = "학년") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반") @RequestParam(required = false) classNumber: Int?
    ): List<AttendanceStudentResponse> {
        return teacherAttendanceService.getAllCheckpointAttendances(
            date = date,
            roomId = roomId,
            statusId = statusId,
            grade = grade,
            classNumber = classNumber
        )
    }
}
