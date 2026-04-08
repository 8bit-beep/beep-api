package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.properties.DomainProperties
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
    private val jwtProvider: JwtProvider,
    private val domainProperties: DomainProperties
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val attributes = oauth2User.attributes

        val roles = attributes["roles"] as? List<*>
            ?: throw CustomException(AuthError.NULL_ROLE)
        val roleString = roles.firstOrNull() as? String
            ?: throw CustomException(AuthError.NULL_ROLE)

        val username = attributes["username"] as? String
            ?: throw CustomException(AuthError.NULL_NAME)

        val name = attributes["name"] as? String
            ?: throw CustomException(AuthError.NULL_NAME)

        val publicId = attributes["publicId"] as? String
            ?: throw CustomException(AuthError.NULL_ROLE)

        val studentMap = attributes["student"] as? Map<*, *>
        val student = if (studentMap != null) {
            com.b.beep.domain.auth.infrastructure.StudentInfo(
                grade = (studentMap["grade"] as Number).toInt(),
                room = (studentMap["room"] as Number).toInt(),
                number = (studentMap["number"] as Number).toInt(),
                isGraduated = studentMap["isGraduated"] as? Boolean ?: false
            )
        } else null

        val dauthUser = DAuthUser(
            publicId = publicId,
            username = username,
            name = name,
            profileImage = attributes["profileImage"] as? String,
            roles = listOf(roleString),
            status = attributes["status"] as? String ?: "ACTIVE",
            student = student
        )

        val user = studentInfoService.getOrCreateUser(dauthUser)

        if (user.role == UserRole.STUDENT) {
            studentInfoService.getOrCreateStudentInfo(user, dauthUser)
            studentInfoService.updateStudentInfo(user, dauthUser)
        }
        val tokens = jwtProvider.generateToken(user.publicId!!)
        val refreshToken = tokens.refreshToken
        val accessToken = tokens.accessToken

        SecurityContextHolder.clearContext()
        val redirectUrl =
            "${domainProperties.web}/callback/dauth" +
            "?refreshToken=$refreshToken&accessToken=$accessToken"

        response.sendRedirect(redirectUrl)
    }
}
