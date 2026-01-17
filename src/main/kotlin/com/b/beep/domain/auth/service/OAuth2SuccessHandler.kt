package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val attributes = oauth2User.attributes

        val roleString = attributes["role"] as? String
            ?: throw CustomException(AuthError.NULL_ROLE)

        val role = UserRole.valueOf(roleString)

        val email = attributes["email"] as? String
            ?: throw CustomException(AuthError.NULL_EMAIL)

        val name = attributes["name"] as? String
            ?: throw CustomException(AuthError.NULL_NAME)

        val (grade, room, number) =
            if (role == UserRole.STUDENT) {
                Triple(
                    (attributes["grade"] as? Number)?.toInt()
                        ?: throw CustomException(AuthError.NULL_STU_NUM),
                    (attributes["room"] as? Number)?.toInt()
                        ?: throw CustomException(AuthError.NULL_STU_NUM),
                    (attributes["number"] as? Number)?.toInt()
                        ?: throw CustomException(AuthError.NULL_STU_NUM)
                )
            } else {
                Triple(null, null, null)
            }

        val dauthUser = DAuthUser(
            id = attributes["sub"] as String,
            name = name,
            email = email,
            profileImage = attributes["profile_image"] as? String,
            role = roleString,
            phone = attributes["phone"] as? String,
            grade = grade,
            room = room,
            number = number
        )

        val user = studentInfoService.getOrCreateUser(dauthUser)

        if (user.role == UserRole.STUDENT) {
            studentInfoService.getOrCreateStudentInfo(user, dauthUser)
        }
        val tokens = jwtProvider.generateToken(user.email)
        val refreshToken = tokens.refreshToken
        val accessToken = tokens.accessToken

        SecurityContextHolder.clearContext()
        response.sendRedirect("https://beep.cher1shrxd.me/callback/dauth?refreshToken=$refreshToken&accessToken=$accessToken")
    }
}
