package com.b.beep.domain.auth.controller

import com.b.beep.domain.auth.controller.docs.DAuthDocs
import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.controller.dto.response.TestResponse
import com.b.beep.domain.auth.service.AuthService
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import com.b.beep.global.common.dto.response.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dauth")
class DAuthController(
    private val authService: AuthService
) : DAuthDocs {
//    @PostMapping("/token")
//    override fun login(@RequestBody request: LoginRequest): ResponseEntity<BaseResponse<TokenResponse>> {
//        return BaseResponse.of(authService.login())
//    }

    @GetMapping("/home")
    fun home(@AuthenticationPrincipal oauth2User: OAuth2User, model: Model): TestResponse {
        model. addAttribute("name", oauth2User.getAttribute<String>("name"))
        model.addAttribute("email", oauth2User.getAttribute<String>("email"))
        model.addAttribute("profileImage", oauth2User.getAttribute<String>("profile_image"))
        model.addAttribute("role", oauth2User.getAttribute<String>("role"))

        val user = TestResponse(
            name = oauth2User.getAttribute("name")!!,
            email = oauth2User.getAttribute("email")!!,
            profileImage = oauth2User.getAttribute("profile_image"),
            role = oauth2User.getAttribute("role")
        )

        return user
    }
}
