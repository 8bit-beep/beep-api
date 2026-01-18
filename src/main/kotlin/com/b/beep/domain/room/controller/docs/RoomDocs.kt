package com.b.beep.domain.room.controller.docs

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "실", description = "실 API")
interface RoomDocs {
    @Operation(summary = "실 생성")
    fun createRoom(@Valid request: CreateRoomRequest): RoomResponse

    @Operation(summary = "실 전체 조회")
    fun getRooms(): List<RoomResponse>

    @Operation(summary = "실 상세 조회")
    fun getRoom(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long): RoomResponse

    @Operation(summary = "실 수정")
    fun updateRoom(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long, @Valid request: UpdateRoomRequest): RoomResponse

    @Operation(summary = "실 삭제")
    fun deleteRoom(@Positive(message = "실 ID는 양수여야 합니다") roomId: Long)
}
