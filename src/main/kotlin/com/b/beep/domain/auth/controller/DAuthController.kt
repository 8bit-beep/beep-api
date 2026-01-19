package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.controller.dto.response.TestResponse
import com.b.beep.domain.auth.service.DAuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Tag(name = "도담 로그인", description = "도담 OAuth 로그인 API")
@RestController
@RequestMapping("/dauth")
class DAuthController(
    private val dauthService: DAuthService,
) {
    @Operation(summary = "로그인", description = "도담 OAuth를 통해 로그인합니다.")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody request: LoginRequest): TokenResponse =
        dauthService.login(request)

    @GetMapping("/home")
    @ResponseStatus(HttpStatus.OK)
    fun home(@AuthenticationPrincipal oauth2User: OAuth2User, model: Model): TestResponse {
        model.addAttribute("name", oauth2User.getAttribute<String>("name"))
        model.addAttribute("email", oauth2User.getAttribute<String>("email"))
        model.addAttribute("profileImage", oauth2User.getAttribute<String>("profile_image"))
        model.addAttribute("role", oauth2User.getAttribute<String>("role"))

        return TestResponse(
            name = oauth2User.getAttribute("name")!!,
            email = oauth2User.getAttribute("email")!!,
            profileImage = oauth2User.getAttribute("profile_image"),
            role = oauth2User.getAttribute("role")
        )
    }
}
