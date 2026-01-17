package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.docs.AuthDocs
import com.b.beep.domain.auth.controller.dto.request.RefreshTokenRequest
import com.b.beep.domain.auth.service.AuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) : AuthDocs {
    @PostMapping("/refresh")
    override fun refresh(@Valid @RequestBody request: RefreshTokenRequest): TokenResponse {
        return authService.refresh(request.refreshToken)
    }
}
