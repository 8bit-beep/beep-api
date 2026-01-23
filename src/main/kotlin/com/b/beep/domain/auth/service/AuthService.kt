package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.AdminLoginRequest
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.repository.RefreshTokenRepository
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtExtractor
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import com.b.beep.global.security.jwt.error.JwtError
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtExtractor: JwtExtractor,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
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

    fun login(request: AdminLoginRequest): TokenResponse {
        val user = userRepository.findByEmailAndIsDeletedFalse(request.email)
            ?: throw CustomException(UserError.USER_NOT_FOUND)

        if (user.password == null) {
            throw CustomException(AuthError.OAUTH_ONLY_ACCOUNT)
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw  CustomException(AuthError.PASSWORD_MISMATCH)
        }

        val newTokens = jwtProvider.generateToken(request.email)

        return newTokens
    }
}
