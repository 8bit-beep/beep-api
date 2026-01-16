package com.b.beep.domain.room.controller.docs

import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "실 승인", description = "실 승인 API")
interface ApprovalDocs {
    @Operation(summary = "실 승인")
    fun createApproval(@PathVariable roomId: Long)

    @Operation(summary = "실 승인 취소")
    fun deleteApproval(@PathVariable roomId: Long)

    @Operation(summary = "승인 상태 전체 조회")
    fun getApprovals(@RequestParam approved: Boolean?): List<RoomApprovalResponse>

    @Operation(summary = "실별 승인 상태 조회")
    fun getApproval(@PathVariable roomId: Long): RoomApprovalResponse
}
