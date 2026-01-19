package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    fun getMe(): UserResponse {
        return userService.getMe()
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다. (soft delete)")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable userId: Long) {
        userService.deleteUser(userId)
    }
}
