package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.domain.AuthError
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
    private val jwtExtractor: JwtExtractor
) {
//    fun login(): TokenResponse {
//        val dauthUser: DAuthUser = run {
//            val auth = SecurityContextHolder.getContext().authentication
//            if (auth?.principal is CustomOAuth2User) {
//                (auth.principal as CustomOAuth2User).dauthUser
//            } else throw CustomException(AuthError.DAUTH_LOGIN)
//        }
//        val user = studentInfoService.getOrCreateUser(dauthUser)
//
//        if (user.role == UserRole.STUDENT) {
//            studentInfoService.getOrCreateStudentInfo(user, dauthUser)
//        }
//
//        return jwtProvider.generateToken(user.email)
//    }

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
