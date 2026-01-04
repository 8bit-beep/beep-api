package com.b.beep.domain.room.approval.controller

import com.b.beep.domain.room.approval.controller.docs.ApprovalDocs
import com.b.beep.domain.room.approval.controller.dto.request.ApproveRequest
import com.b.beep.domain.room.approval.controller.dto.response.ApprovalResponse
import com.b.beep.domain.room.approval.service.ApprovalService
import com.b.beep.domain.attendance.domain.enums.Room
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/approve")
class ApprovalController(
    private val approvalService: ApprovalService
) : ApprovalDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    override fun approve(@RequestBody request: ApproveRequest) {
        approvalService.approve(request)
    }

    @GetMapping("/not")
    @ResponseStatus(HttpStatus.OK)
    override fun getAllNotApprovedRooms(): List<ApprovalResponse> {
        return approvalService.getNotApprovedRooms()
    }

    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getApprovalStatusByRoom(@PathVariable roomId: Long): ApprovalResponse {
        return approvalService.getApprovalStatusByRoom(roomId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getAllApprovals(): List<ApprovalResponse> {
        return approvalService.getAllApprovalStatus()
    }
}
