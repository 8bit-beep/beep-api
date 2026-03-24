package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceReasonResponse
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.domain.enums.AbsenceReason
import com.b.beep.domain.absence.controller.dto.response.CreateAbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.UpdateAbsenceResponse
import com.b.beep.domain.absence.service.AbsenceService
import com.b.beep.global.common.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "외박자 관리", description = "외박자 관리 API")
@Validated
@RestController
@RequestMapping("/absences")
class AbsenceController(
    private val absenceService: AbsenceService,
) {

    @Operation(summary = "결석 사유 타입 목록 조회")
    @GetMapping("/reasons")
    @ResponseStatus(HttpStatus.OK)
    fun getAbsenceReasons(): AbsenceReasonResponse {
        return AbsenceReasonResponse(AbsenceReason.entries.map { it.name })
    }

    @Operation(
        summary = "외박자 생성",
        description = """
            외박자를 생성합니다.

            - 선택된 사용자 중 기간이 겹치는 경우 해당 유저를 제외하고 생성합니다.
            - typeId: 출석 타입 ID (필수)
              - 외박 또는 외출 AttendanceType의 ID를 입력하세요.
        """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAbsence(@Valid @RequestBody request: CreateAbsenceRequest): CreateAbsenceResponse {
        return absenceService.createAbsence(request)
    }

    @Operation(summary = "전체 외박자 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAbsences(
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable
    ): PageResponse<AbsenceResponse> {
        return PageResponse.from(absenceService.getAbsences(pageable))
    }

    @Operation(summary = "오늘 외박자 조회")
    @GetMapping("/today")
    @ResponseStatus(HttpStatus.OK)
    fun getAbsencesToday(
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable
    ): PageResponse<AbsenceResponse> {
        return PageResponse.from(absenceService.getAbsencesToday(pageable))
    }

    @Operation(
        summary = "외박자 수정",
        description = """
            외박자를 수정합니다.

            - typeId: 출석 타입 ID (필수)
              - 외박 또는 외출 AttendanceType의 ID를 입력하세요.
        """
    )
    @PatchMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateAbsence(
        @PathVariable @Positive(message = "결석 ID는 양수여야 합니다") absenceId: Long,
        @Valid @RequestBody request: UpdateAbsenceRequest
    ): UpdateAbsenceResponse {
        return absenceService.updateAbsence(absenceId, request)
    }

    @Operation(summary = "외박자 삭제")
    @DeleteMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAbsence(
        @PathVariable @Positive(message = "부재 ID는 양수여야 합니다") absenceId: Long
    ) {
        absenceService.deleteAbsence(absenceId)
    }
}
