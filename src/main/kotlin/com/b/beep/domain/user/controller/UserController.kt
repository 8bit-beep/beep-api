package com.b.beep.domain.user.controller

import com.b.beep.domain.user.controller.docs.UserDocs
import com.b.beep.domain.user.controller.dto.response.UserResponse
import com.b.beep.domain.user.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) : UserDocs {
    @GetMapping("/my")
    override fun getMe(): UserResponse {
        return userService.getMe()
    }
}