package com.b.beep.domain.absence.controller.docs

import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.CreateAbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.UpdateAbsenceResponse
import com.b.beep.global.common.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "장기결석", description = "장기결석 API")
interface AbsenceDocs {
    @Operation(
        summary = "장기결석 생성",
        description = """
            장기결석을 생성합니다.

            - 선택된 사용자 중 결석 생성이 실패한 경우 해당 유저를 제외하고 생성합니다.
            - typeId: 출석 타입 ID (선택)
              - 지정 시: 모든 출석 기록에 해당 타입 적용
              - 미지정 시: 학생 스케줄 기반 타입 적용 (스케줄 없으면 SLEEPOVER)
        """
    )
    fun createAbsence(@RequestBody request: CreateAbsenceRequest): CreateAbsenceResponse

    @Operation(summary = "모든 장기결석 조회")
    fun getAbsences(pageable: Pageable): PageResponse<AbsenceResponse>

    @Operation(
        summary = "장기결석 수정",
        description = """
            장기결석을 수정합니다.

            - typeId: 출석 타입 ID (선택)
              - 지정 시: 모든 출석 기록에 해당 타입 적용
              - 미지정 시: 학생 스케줄 기반 타입 적용 (스케줄 없으면 SLEEPOVER)
        """
    )
    fun updateAbsence(@PathVariable absenceId: Long, @RequestBody request: UpdateAbsenceRequest): UpdateAbsenceResponse

    @Operation(summary = "장기결석 삭제")
    fun deleteAbsence(@PathVariable absenceId: Long)
}
