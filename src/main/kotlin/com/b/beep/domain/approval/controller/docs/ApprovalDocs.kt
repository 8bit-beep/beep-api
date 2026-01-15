package com.b.beep.domain.approval.controller.docs

import com.b.beep.domain.approval.controller.dto.request.ApproveRequest
import com.b.beep.domain.approval.controller.dto.response.ApprovalResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "실 승인", description = "실 승인 API")
interface ApprovalDocs {
    @Operation(summary = "실 승인 또는 취소")
    fun approve(request: ApproveRequest)

    @Operation(summary = "현재 미승인 실 전체 조회")
    fun getAllNotApprovedRooms(): List<ApprovalResponse>

    @Operation(summary = "실 별 승인 상태 조회")
    fun getApprovalStatusByRoom(roomId: Long): ApprovalResponse

    @Operation(summary = "현재 승인 상태 전체 조회")
    fun getAllApprovals(): List<ApprovalResponse>
}