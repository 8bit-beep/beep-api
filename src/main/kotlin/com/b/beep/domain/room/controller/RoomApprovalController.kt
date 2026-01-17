package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.docs.RoomApprovalDocs
import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import com.b.beep.domain.room.service.RoomApprovalService
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping
class RoomApprovalController(
    private val roomApprovalService: RoomApprovalService
) : RoomApprovalDocs {
    @PostMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomApprovalService.createApproval(roomId)
    }

    @GetMapping("/approvals")
    @ResponseStatus(HttpStatus.OK)
    override fun getApprovals(@RequestParam approved: Boolean?): List<RoomApprovalResponse> {
        return roomApprovalService.getApprovals(approved)
    }

    @GetMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.OK)
    override fun getApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ): RoomApprovalResponse {
        return roomApprovalService.getApproval(roomId)
    }

    @DeleteMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteApproval(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomApprovalService.deleteApproval(roomId)
    }
}
