package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.dto.request.AdminLoginRequest
import com.b.beep.domain.auth.controller.dto.request.RefreshTokenRequest
import com.b.beep.domain.auth.service.AuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "인증 관련 API")
@Validated
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(summary = "토큰 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): TokenResponse {
        return authService.refresh(request.refreshToken)
    }

    @Operation(summary = "어드민 로그인", description = "어드민 전용 로그인 입니다")
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    fun signin(@Valid @RequestBody request: AdminLoginRequest): TokenResponse{
        return authService.login(request)
    }
}
