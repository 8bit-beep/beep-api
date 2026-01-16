package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.docs.RoomDocs
import com.b.beep.domain.room.controller.dto.request.RoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.service.RoomService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService
) : RoomDocs {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createRoom(@RequestBody request: RoomRequest): RoomResponse {
        return roomService.createRoom(request)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    override fun getRooms(): List<RoomResponse> {
        return roomService.getRooms()
    }

    @GetMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    override fun getRoom(@PathVariable roomId: Long): RoomResponse {
        return roomService.getRoom(roomId)
    }

    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    override fun updateRoom(
        @PathVariable roomId: Long,
        @RequestBody request: RoomRequest
    ): RoomResponse {
        return roomService.updateRoom(roomId, request)
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteRoom(@PathVariable roomId: Long) {
        roomService.deleteRoom(roomId)
    }
}
