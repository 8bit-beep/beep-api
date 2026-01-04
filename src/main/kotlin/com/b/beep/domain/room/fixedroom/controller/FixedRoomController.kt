package com.b.beep.domain.room.fixedroom.controller

import com.b.beep.domain.room.fixedroom.controller.dto.response.FixedRoomResponse
import com.b.beep.domain.room.fixedroom.controller.docs.FixedRoomDocs
import com.b.beep.domain.room.fixedroom.controller.dto.request.CreateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.controller.dto.request.UpdateFixedRoomRequest
import com.b.beep.domain.room.fixedroom.service.FixedRoomService
import com.b.beep.global.common.dto.response.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/fixed-rooms")
class FixedRoomController(
    private val fixedRoomService: FixedRoomService,
) : FixedRoomDocs {
    @PostMapping
    override fun createFixedRoom(@RequestBody request: CreateFixedRoomRequest) {
        fixedRoomService.createFixedRoom(request)
    }

    @GetMapping
    override fun getFixedRooms(): ResponseEntity<BaseResponse<List<FixedRoomResponse>>> {
        return BaseResponse.of(fixedRoomService.getAll())
    }

    @PatchMapping("/{fixedRoomId}")
    override fun updateFixedRoom(@PathVariable fixedRoomId: Long, @RequestBody request: UpdateFixedRoomRequest) {
        fixedRoomService.update(fixedRoomId, request)
    }

    @DeleteMapping("/{fixedRoomId}")
    override fun deleteFixedRoom(@PathVariable fixedRoomId: Long) {
        fixedRoomService.delete(fixedRoomId)
    }
}