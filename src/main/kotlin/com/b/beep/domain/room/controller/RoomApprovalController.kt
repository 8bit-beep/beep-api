package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import com.b.beep.domain.room.service.RoomApprovalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "실 승인", description = "실 승인 API")
@Validated
@RestController
@RequestMapping
class RoomApprovalController(
    private val roomApprovalService: RoomApprovalService
) {
    @Operation(summary = "실 승인")
    @PostMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    fun createApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomApprovalService.createApproval(roomId)
    }

    @Operation(summary = "승인 상태 전체 조회")
    @GetMapping("/approvals")
    @ResponseStatus(HttpStatus.OK)
    fun getApprovals(@RequestParam approved: Boolean?): List<RoomApprovalResponse> {
        return roomApprovalService.getApprovals(approved)
    }

    @Operation(summary = "실별 승인 상태 조회")
    @GetMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.OK)
    fun getApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ): RoomApprovalResponse {
        return roomApprovalService.getApproval(roomId)
    }

    @Operation(summary = "실 승인 취소")
    @DeleteMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomApprovalService.deleteApproval(roomId)
    }
}
