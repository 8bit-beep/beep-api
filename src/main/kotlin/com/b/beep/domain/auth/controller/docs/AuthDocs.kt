package com.b.beep.domain.auth.controller.docs

import com.b.beep.domain.auth.controller.dto.request.RefreshTokenRequest
import com.b.beep.global.common.dto.response.BaseResponse
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "인증", description = "인증 관련 API")
interface AuthDocs {
    @Operation(summary = "토큰 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.")
    fun refresh(request: RefreshTokenRequest): ResponseEntity<BaseResponse<TokenResponse>>
}
