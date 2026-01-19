package com.b.beep.domain.checkpoint.controller

import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import com.b.beep.domain.checkpoint.service.AttendanceCheckpointService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "출석 체크포인트", description = "출석 체크포인트 관리 API")
@Validated
@RestController
@RequestMapping("/checkpoints")
class AttendanceCheckpointController(
    private val checkpointService: AttendanceCheckpointService
) {
    @Operation(summary = "체크포인트 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCheckpoint(@Valid @RequestBody request: CreateCheckpointRequest) {
        checkpointService.createCheckpoint(request)
    }

    @Operation(summary = "체크포인트 목록 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCheckpoints(): List<CheckpointResponse> {
        return checkpointService.getCheckpoints()
    }

    @Operation(summary = "체크포인트 상세 조회")
    @GetMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.OK)
    fun getCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long
    ): CheckpointResponse {
        return checkpointService.getCheckpoint(checkpointId)
    }

    @Operation(summary = "체크포인트 수정")
    @PatchMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long,
        @Valid @RequestBody request: UpdateCheckpointRequest
    ) {
        checkpointService.updateCheckpoint(checkpointId, request)
    }

    @Operation(summary = "체크포인트 삭제")
    @DeleteMapping("/{checkpointId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCheckpoint(
        @PathVariable @Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long
    ) {
        checkpointService.deleteCheckpoint(checkpointId)
    }
}
