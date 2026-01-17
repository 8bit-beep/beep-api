package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.docs.RoomDocs
import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.service.RoomService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService
) : RoomDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createRoom(@Valid @RequestBody request: CreateRoomRequest): RoomResponse {
        return roomService.createRoom(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getRooms(): List<RoomResponse> {
        return roomService.getRooms()
    }

    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ): RoomResponse {
        return roomService.getRoom(roomId)
    }

    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long,
        @Valid @RequestBody request: UpdateRoomRequest
    ): RoomResponse {
        return roomService.updateRoom(roomId, request)
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteRoom(
        @PathVariable @Positive(message = "실 ID는 양수여야 합니다") roomId: Long
    ) {
        roomService.deleteRoom(roomId)
    }
}
