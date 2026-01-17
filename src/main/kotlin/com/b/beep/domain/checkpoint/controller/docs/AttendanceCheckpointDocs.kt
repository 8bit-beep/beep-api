package com.b.beep.domain.checkpoint.controller.docs

import com.b.beep.domain.checkpoint.controller.dto.request.CreateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.request.UpdateCheckpointRequest
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "출석 체크포인트", description = "출석 체크포인트 관리 API")
interface AttendanceCheckpointDocs {
    @Operation(summary = "체크포인트 생성")
    fun createCheckPoint(request: CreateCheckpointRequest)

    @Operation(summary = "체크포인트 수정")
    fun updateCheckPoint(checkPointId: Long, request: UpdateCheckpointRequest)

    @Operation(summary = "체크포인트 목록 조회")
    fun findCheckPoints(): List<CheckpointResponse>

    @Operation(summary = "체크포인트 상세 조회")
    fun findCheckPoint(checkPointId: Long): CheckpointResponse

    @Operation(summary = "체크포인트 삭제")
    fun deleteCheckPoint(checkPointId: Long)
}
