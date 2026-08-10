package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.dto.request.DodamMiniAppLoginRequest
import com.b.beep.domain.auth.service.DodamMiniAppAuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "AID인증", description = "AID인증 관련 API")
@RestController
@RequestMapping("/auth")
class DodamMiniAppAuthController(
    private val dodamMiniAppAuthService: DodamMiniAppAuthService,
) {
    @Operation(summary = "App in Dodam 로그인", description = "WebView에서 전달받은 도담 토큰으로 로그인합니다.")
    @PostMapping("/dodam")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody request: DodamMiniAppLoginRequest): TokenResponse {
        return dodamMiniAppAuthService.login(request.token)
    }
}
