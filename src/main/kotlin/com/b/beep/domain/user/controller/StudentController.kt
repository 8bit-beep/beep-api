package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.dto.request.StudentActivityRoomRequest
import com.b.beep.domain.user.controller.dto.response.StudentActivityRoomResponse
import com.b.beep.domain.user.controller.dto.response.StudentResponse
import com.b.beep.domain.user.service.StudentActivityRoomService
import com.b.beep.domain.user.service.StudentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "학생", description = "학생 API")
@Validated
@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService,
    private val studentActivityRoomService: StudentActivityRoomService
) {

    @Operation(summary = "학생 검색", description = "학년, 반, 이름으로 학생을 검색합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getStudents(
        @Parameter(description = "학년") @RequestParam(required = false) grade: Int?,
        @Parameter(description = "반") @RequestParam(required = false) classNumber: Int?,
        @Parameter(description = "이름 검색 키워드") @RequestParam(required = false) keyword: String?
    ): List<StudentResponse> {
        return studentService.findStudents(grade, classNumber, keyword)
    }

    @Operation(summary = "학생 활동 실 조회", description = "특정 학생의 요일별 활동 실 배정을 조회합니다.")
    @GetMapping("/{studentId}/activity-rooms")
    @ResponseStatus(HttpStatus.OK)
    fun getActivityRooms(
        @Parameter(description = "학생 ID") @PathVariable @Positive(message = "학생 ID는 양수여야 합니다") studentId: Long
    ): List<StudentActivityRoomResponse> {
        return studentActivityRoomService.getActivityRooms(studentId)
    }

    @Operation(summary = "학생 활동 실 저장", description = "특정 학생의 요일별 활동 실 배정을 전체 교체합니다.")
    @PutMapping("/{studentId}/activity-rooms")
    @ResponseStatus(HttpStatus.OK)
    fun replaceActivityRooms(
        @Parameter(description = "학생 ID") @PathVariable @Positive(message = "학생 ID는 양수여야 합니다") studentId: Long,
        @Valid @RequestBody requests: List<StudentActivityRoomRequest>
    ): List<StudentActivityRoomResponse> {
        return studentActivityRoomService.replaceActivityRooms(studentId, requests)
    }
}
