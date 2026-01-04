package com.b.beep.domain.room.controller.docs

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "실", description = "실 관련 API")
interface RoomDocs {
    @Operation(summary = "실 추가", description = "새로운 실을 추가합니다.")
    fun createRoom(request: CreateRoomRequest)

    @Operation(summary = "실 목록 조회", description = "전체 실 목록을 조회합니다.")
    fun getRooms(): List<RoomResponse>

    @Operation(summary = "실 수정", description = "실을 수정합니다.")
    fun updateRoom(roomId: Long, request: UpdateRoomRequest)

    @Operation(summary = "실 삭제", description = "실을 삭제합니다.")
    fun deleteRoom(roomId: Long)
}
