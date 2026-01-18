package com.b.beep.domain.checkpoint.controller.docs

import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "출석 체크포인트", description = "출석 체크포인트 관리 API")
interface AttendanceCheckpointDocs {
    @Operation(summary = "체크포인트 생성")
    fun createCheckpoint(@Valid request: CreateCheckpointRequest)

    @Operation(summary = "체크포인트 수정")
    fun updateCheckpoint(@Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long, @Valid request: UpdateCheckpointRequest)

    @Operation(summary = "체크포인트 목록 조회")
    fun getCheckpoints(): List<CheckpointResponse>

    @Operation(summary = "체크포인트 상세 조회")
    fun getCheckpoint(@Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long): CheckpointResponse

    @Operation(summary = "체크포인트 삭제")
    fun deleteCheckpoint(@Positive(message = "체크포인트 ID는 양수여야 합니다") checkpointId: Long)
}
