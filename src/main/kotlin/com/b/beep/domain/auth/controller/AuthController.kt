package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.docs.AuthDocs
import com.b.beep.domain.auth.controller.dto.request.RefreshTokenRequest
import com.b.beep.domain.auth.service.AuthService
import com.b.beep.global.common.dto.response.BaseResponse
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) : AuthDocs {
    @PostMapping("/refresh")
    override fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<BaseResponse<TokenResponse>> {
        val refreshToken = request.refreshToken
        return BaseResponse.of(authService.refresh(refreshToken))
    }
}
