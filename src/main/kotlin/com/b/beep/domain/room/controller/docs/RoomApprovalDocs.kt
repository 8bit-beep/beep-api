package com.b.beep.domain.room.controller.docs

import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive

@Tag(name = "실 승인", description = "실 승인 API")
interface RoomApprovalDocs {
    @Operation(summary = "실 승인")
    fun createApproval(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long)

    @Operation(summary = "승인 상태 전체 조회")
    fun getApprovals(approved: Boolean?): List<RoomApprovalResponse>

    @Operation(summary = "실별 승인 상태 조회")
    fun getApproval(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long): RoomApprovalResponse

    @Operation(summary = "실 승인 취소")
    fun deleteApproval(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long)
}
