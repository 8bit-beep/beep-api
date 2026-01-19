package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.service.RoomService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "실", description = "실 API")
@Validated
@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService
) {
    @Operation(summary = "실 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(@Valid @RequestBody request: CreateRoomRequest): RoomResponse {
        return roomService.createRoom(request)
    }

    @Operation(summary = "실 전체 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getRooms(): List<RoomResponse> {
        return roomService.getRooms()
    }

    @Operation(summary = "실 상세 조회")
    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    fun getRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ): RoomResponse {
        return roomService.getRoom(roomId)
    }

    @Operation(summary = "실 수정")
    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long,
        @Valid @RequestBody request: UpdateRoomRequest
    ): RoomResponse {
        return roomService.updateRoom(roomId, request)
    }

    @Operation(summary = "실 삭제")
    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomService.deleteRoom(roomId)
    }
}
