package com.b.beep.domain.attendance.controller

import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceSortModeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceSortModesResponse
import com.b.beep.domain.attendance.service.AttendanceSortModeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "출석 재정렬 모드", description = "학년별 출석 명단 재정렬 모드 API")
@Validated
@RestController
@RequestMapping("/attendance-sort-modes")
class AttendanceSortModeController(
    private val attendanceSortModeService: AttendanceSortModeService
) {
    @Operation(summary = "재정렬 모드 조회", description = "오늘 날짜와 현재/가까운 체크포인트 기준으로 학년별 변경값(type)과 요일별 기본값(defaultType)을 조회합니다. 기본 선택값은 type이 없으면 defaultType을 사용합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSortModes(): AttendanceSortModesResponse {
        return attendanceSortModeService.getSortModes()
    }

    @Operation(summary = "재정렬 모드 변경", description = "오늘 날짜와 현재/가까운 체크포인트 기준으로 학년 하나의 재정렬 모드를 변경합니다. typeId가 null이면 변경값을 삭제하고 기본 스케줄로 복원합니다.")
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    fun updateSortMode(
        @Valid @RequestBody request: UpdateAttendanceSortModeRequest
    ): AttendanceSortModesResponse {
        return attendanceSortModeService.updateSortMode(request)
    }
}
