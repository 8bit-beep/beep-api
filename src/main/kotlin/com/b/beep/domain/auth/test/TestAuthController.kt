package com.b.beep.domain.auth.test

import com.b.beep.domain.auth.test.TestLoginRequest
import com.b.beep.domain.auth.test.TestAuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("dev", "local")
@RestController
@RequestMapping("/test")
class TestAuthController(
    private val testAuthService: TestAuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: TestLoginRequest): TokenResponse =
        testAuthService.login(request)
}