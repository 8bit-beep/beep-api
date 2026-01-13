package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.repository.RefreshTokenRepository
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.global.security.jwt.JwtExtractor
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import com.b.beep.global.security.jwt.error.JwtError
import com.b.beep.global.exception.CustomException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtExtractor: JwtExtractor,
    private val dAuthService: DAuthService,
) {
    fun login(request: LoginRequest): TokenResponse {
        val token = dAuthService.getDAuthToken(request.code)
        val dodamUser = dAuthService.getDAuthUser(token)
        val user = studentInfoService.getOrCreateUser(dodamUser)

        if (user.role == UserRole.STUDENT) {
            studentInfoService.getOrCreateStudentInfo(user, dodamUser)
        }

        return jwtProvider.generateToken(user.email)
    }


    fun refresh(refreshToken: String): TokenResponse {
        val email = jwtExtractor.getEmail(refreshToken)

        val savedToken = refreshTokenRepository.findByUserId(email)
            ?: throw CustomException(JwtError.REFRESH_TOKEN_NOT_FOUND)
        if (savedToken != refreshToken) {
            throw CustomException(JwtError.INVALID_REFRESH_TOKEN)
        }

        val newTokens = jwtProvider.generateToken(email)

        return newTokens
    }
}
