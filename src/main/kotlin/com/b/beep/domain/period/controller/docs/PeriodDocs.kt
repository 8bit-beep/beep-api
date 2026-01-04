package com.b.beep.domain.period.controller.docs

import com.b.beep.domain.period.controller.dto.request.CreatePeriodRequest
import com.b.beep.domain.period.controller.dto.request.UpdatePeriodRequest
import com.b.beep.domain.period.controller.dto.response.PeriodResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "교시", description = "교시 관련 API")
interface PeriodDocs {
    @Operation(summary = "교시 추가", description = "새로운 교시를 추가합니다.")
    fun createPeriod(request: CreatePeriodRequest)

    @Operation(summary = "교시 목록 조회", description = "전체 교시 목록을 조회합니다.")
    fun getPeriods(): List<PeriodResponse>

    @Operation(summary = "교시 단일 조회", description = "교시를 조회합니다.")
    fun getPeriod(periodId: Long): PeriodResponse

    @Operation(summary = "교시 수정", description = "교시를 수정합니다.")
    fun updatePeriod(periodId: Long, request: UpdatePeriodRequest)

    @Operation(summary = "교시 삭제", description = "교시를 삭제합니다.")
    fun deletePeriod(periodId: Long)

    @Operation(summary = "현재 출석 교시 조회", description = "현재 출석 가능한 교시를 조회합니다.")
    fun getCurrentAttendancePeriod(): Int

    @Operation(summary = "현재 교시 조회", description = "현재 교시를 조회합니다.")
    fun getCurrentPeriod(): Int
}
