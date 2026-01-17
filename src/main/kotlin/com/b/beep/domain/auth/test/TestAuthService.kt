package com.b.beep.domain.auth.test

import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("dev", "local")
@Service
@Transactional
class TestAuthService(
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider
) {
    fun login(request: TestLoginRequest): TokenResponse {
        val dauthUser = DAuthUser(
            id = "test-${request.email}",
            name = request.name,
            email = request.email,
            role = request.role,
            grade = request.grade,
            room = request.classNumber,
            number = request.num
        )

        val user = studentInfoService.getOrCreateUser(dauthUser)

        if (user.role == UserRole.STUDENT && request.grade != null) {
            studentInfoService.getOrCreateStudentInfo(user, dauthUser)
        }

        return jwtProvider.generateToken(user.email)
    }
}