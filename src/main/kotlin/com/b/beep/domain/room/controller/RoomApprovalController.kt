package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.docs.RoomApprovalDocs
import com.b.beep.domain.room.controller.dto.response.RoomApprovalResponse
import com.b.beep.domain.room.service.RoomApprovalService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class RoomRoomApprovalController(
    private val roomApprovalService: RoomApprovalService
) : RoomApprovalDocs {
    @PostMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createApproval(@PathVariable roomId: Long) {
        roomApprovalService.createApproval(roomId)
    }

    @GetMapping("/approvals")
    @ResponseStatus(HttpStatus.OK)
    override fun getApprovals(@RequestParam approved: Boolean?): List<RoomApprovalResponse> {
        return roomApprovalService.getApprovals(approved)
    }

    @GetMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.OK)
    override fun getApproval(@PathVariable roomId: Long): RoomApprovalResponse {
        return roomApprovalService.getApproval(roomId)
    }

    @DeleteMapping("/rooms/{roomId}/approvals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteApproval(@PathVariable roomId: Long) {
        roomApprovalService.deleteApproval(roomId)
    }
}