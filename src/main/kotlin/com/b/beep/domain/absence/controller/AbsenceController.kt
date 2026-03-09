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

@Tag(name = "장기결석", description = "장기결석 API")
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
        summary = "장기결석 생성",
        description = """
            장기결석을 생성합니다.

            - 선택된 사용자 중 결석 생성이 실패한 경우 해당 유저를 제외하고 생성합니다.
            - absenceType: 결석 사유 (필수)
              - ILLNESS: 질병
              - FAMILY: 가정사
              - SCHOOL_ACTIVITY: 학교공식활동
              - EXTERNAL_ACTIVITY: 외부대회/활동
              - INSTITUTION_VISIT: (공공)기관 방문
              - PERSONAL: 개인사유
              - OTHER: 기타
              - UNAUTHORIZED: 무단
        """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAbsence(@Valid @RequestBody request: CreateAbsenceRequest): CreateAbsenceResponse {
        return absenceService.createAbsence(request)
    }

    @Operation(summary = "모든 장기결석 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAbsences(
        @PageableDefault(size = 20, sort = ["id"]) pageable: Pageable
    ): PageResponse<AbsenceResponse> {
        return PageResponse.from(absenceService.getAbsences(pageable))
    }

    @Operation(
        summary = "장기결석 수정",
        description = """
            장기결석을 수정합니다.

            - absenceType: 결석 사유 (필수)
              - ILLNESS: 질병
              - FAMILY: 가정사
              - SCHOOL_ACTIVITY: 학교공식활동
              - EXTERNAL_ACTIVITY: 외부대회/활동
              - INSTITUTION_VISIT: (공공)기관 방문
              - PERSONAL: 개인사유
              - OTHER: 기타
              - UNAUTHORIZED: 무단
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

    @Operation(summary = "장기결석 삭제")
    @DeleteMapping("/{absenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAbsence(
        @PathVariable @Positive(message = "부재 ID는 양수여야 합니다") absenceId: Long
    ) {
        absenceService.deleteAbsence(absenceId)
    }
}
