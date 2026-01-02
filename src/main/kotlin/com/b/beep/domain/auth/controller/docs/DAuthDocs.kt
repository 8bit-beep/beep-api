package com.b.beep.domain.auth.controller.docs

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import com.b.beep.global.common.dto.response.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "도담 로그인", description = "도담 OAuth 로그인 API")
interface DAuthDocs {
    @Operation(summary = "로그인", description = "도담 OAuth를 통해 로그인합니다.")
    fun login(request: LoginRequest): ResponseEntity<BaseResponse<TokenResponse>>
}
