package com.b.beep.domain.room.controller

import com.b.beep.domain.room.controller.dto.request.RoomRequest
import com.b.beep.domain.room.controller.dto.response.RoomResponse
import com.b.beep.domain.room.service.RoomService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService
) {
    @GetMapping
    fun findAll(): List<RoomResponse> {
        return roomService.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): RoomResponse {
        return roomService.findById(id)
    }

    @PostMapping
    fun create(@RequestBody request: RoomRequest): RoomResponse {
        return roomService.create(request)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: RoomRequest
    ): RoomResponse {
        return roomService.update(id, request)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        roomService.delete(id)
    }
}
