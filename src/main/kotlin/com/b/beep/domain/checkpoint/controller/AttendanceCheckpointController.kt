package com.b.beep.domain.checkpoint.controller

import com.b.beep.domain.checkpoint.controller.docs.AttendanceCheckpointDocs
import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import com.b.beep.domain.checkpoint.service.AttendanceCheckpointService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/checkpoints")
class AttendanceCheckpointController(
    private val checkpointService: AttendanceCheckpointService
) : AttendanceCheckpointDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createCheckPoint(@RequestBody request: CreateCheckpointRequest) {
        checkpointService.createCheckPoint(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun findCheckPoints(): List<CheckpointResponse> {
        return checkpointService.getCheckPoints()
    }

    @GetMapping("/{checkPointId}")
    @ResponseStatus(HttpStatus.OK)
    override fun findCheckPoint(@PathVariable checkPointId: Long): CheckpointResponse {
        return checkpointService.getCheckPoint(checkPointId)
    }

    @PatchMapping("/{checkPointId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateCheckPoint(@PathVariable checkPointId: Long, @RequestBody request: UpdateCheckpointRequest) {
        checkpointService.updateCheckPoint(checkPointId, request)
    }

    @DeleteMapping("/{checkPointId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteCheckPoint(@PathVariable checkPointId: Long) {
        checkpointService.deleteCheckPoint(checkPointId)
    }
}
