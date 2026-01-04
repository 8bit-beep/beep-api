package com.b.beep.domain.room.fixedroom.controller.docs

import com.b.beep.domain.room.fixedroom.controller.dto.request.CreateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.controller.dto.request.UpdateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.controller.dto.response.FixedRoomResponse
import com.b.beep.global.common.dto.response.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "고정실", description = "고정실 관련 API")
interface FixedRoomDocs {
    @Operation(summary = "고정실 추가", description = "사용자의 고정실을 추가합니다.")
    fun createFixedRoom(request: CreateFixedRoomRequest)

    @Operation(summary = "고정실 목록 조회", description = "사용자의 고정실 목록을 조회합니다.")
    fun getFixedRooms(): ResponseEntity<BaseResponse<List<FixedRoomResponse>>>

    @Operation(summary = "고정실 수정", description = "고정실 정보를 수정합니다.")
    fun updateFixedRoom(fixedRoomId: Long, request: UpdateFixedRoomRequest)

    @Operation(summary = "고정실 삭제", description = "고정실을 삭제합니다.")
    fun deleteFixedRoom(fixedRoomId: Long)
}
