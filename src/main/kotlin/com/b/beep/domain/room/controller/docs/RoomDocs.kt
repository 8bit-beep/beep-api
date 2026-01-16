package com.b.beep.domain.room.controller.docs

import com.b.beep.domain.room.controller.dto.request.RoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "실", description = "실 API")
interface RoomDocs {
    @Operation(summary = "실 생성")
    fun createRoom(@RequestBody request: RoomRequest): RoomResponse

    @Operation(summary = "실 전체 조회")
    fun getRooms(): List<RoomResponse>

    @Operation(summary = "실 상세 조회")
    fun getRoom(@PathVariable roomId: Long): RoomResponse

    @Operation(summary = "실 수정")
    fun updateRoom(@PathVariable roomId: Long, @RequestBody request: RoomRequest): RoomResponse

    @Operation(summary = "실 삭제")
    fun deleteRoom(@PathVariable roomId: Long)
}
