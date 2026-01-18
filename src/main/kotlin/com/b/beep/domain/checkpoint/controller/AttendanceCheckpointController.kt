package com.b.beep.domain.checkpoint.controller

import com.b.beep.domain.checkpoint.controller.docs.AttendanceCheckpointDocs
import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import com.b.beep.domain.checkpoint.service.AttendanceCheckpointService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/checkpoints")
class AttendanceCheckpointController(
    private val checkpointService: AttendanceCheckpointService
) : AttendanceCheckpointDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createCheckpoint(@Valid @RequestBody request: CreateCheckpointRequest) {
        checkpointService.createCheckpoint(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getCheckpoints(): List<CheckpointResponse> {
        return checkpointService.getCheckpoints()
    }

    @GetMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long
    ): CheckpointResponse {
        return checkpointService.getCheckpoint(checkpointId)
    }

    @PatchMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long,
        @Valid @RequestBody request: UpdateCheckpointRequest
    ) {
        checkpointService.updateCheckpoint(checkpointId, request)
    }

    @DeleteMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long
    ) {
        checkpointService.deleteCheckpoint(checkpointId)
    }
}
