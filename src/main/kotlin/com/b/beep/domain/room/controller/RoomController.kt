package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.docs.RoomDocs
import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.service.RoomService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService,
) : RoomDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createRoom(@RequestBody request: CreateRoomRequest) {
        roomService.createRoom(request)
    }

    @GetMapping
    override fun getRooms(): List<RoomResponse> {
        return roomService.getRooms()
    }

    @PatchMapping("/{roomId}")
    override fun updateRoom(
        @PathVariable roomId: Long,
        @RequestBody request: UpdateRoomRequest
    ) {
        roomService.updateRoom(roomId, request)
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteRoom(@PathVariable roomId: Long) {
        roomService.deleteRoom(roomId)
    }
}
