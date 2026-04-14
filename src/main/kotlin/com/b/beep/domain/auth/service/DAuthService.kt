package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthProperties
import com.b.beep.domain.auth.infrastructure.DAuthTokenResponse
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory

@Service
class DAuthService(
    private val dAuthProperties: DAuthProperties,
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
) {
    private val log = LoggerFactory.getLogger(DAuthService::class.java)

    fun login(request: LoginRequest): TokenResponse {
        val token = getDAuthToken(request.code, request.codeVerifier)
        val dodamUser = getDAuthUser(token)
        val user = studentInfoService.getOrCreateUser(dodamUser)

        if (user.role == UserRole.STUDENT) {
            studentInfoService.getOrCreateStudentInfo(user, dodamUser)
            studentInfoService.updateStudentInfo(user, dodamUser)
        }

        return jwtProvider.generateToken(user.publicId!!)
    }

    private fun getDAuthToken(code: String, codeVerifier: String): String {
        val webClient: WebClient = WebClient.create("https://dodam-api.b1nd.com")

        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "code" to code,
                "grant_type" to "authorization_code",
                "redirect_uri" to "https://beep.cher1shrxd.me/callback/dauth",
                "client_id" to dAuthProperties.clientId,
                "client_secret" to dAuthProperties.clientSecret,
                "code_verifier" to codeVerifier
            ))
            .retrieve()
            .onStatus({ it.isError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java).flatMap { body ->
                    log.error(
                        "DAuth token exchange failed. status={}, responseBody={}, codePresent={}, codeLen={}, verifierPresent={}, verifierLen={}, redirectUri={}, grantType={}",
                        clientResponse.statusCode(),
                        body,
                        code.isNotBlank(),
                        code.length,
                        codeVerifier.isNotBlank(),
                        codeVerifier.length,
                        "https://beep.cher1shrxd.me/callback/dauth",
                        "authorization_code"
                    )
                    Mono.error(CustomException(AuthError.TOKEN_FETCH_FAILED))
                }
            }
            .bodyToMono(DAuthTokenResponse::class.java)
            .block()

        return response?.accessToken ?: throw CustomException(AuthError.TOKEN_FETCH_FAILED)
    }

    private fun getDAuthUser(token: String): DAuthUser {
        val webClient: WebClient = WebClient.create("https://dodam-api.b1nd.com")

        val response = webClient.get()
            .uri("/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java).flatMap { body ->
                    log.error(
                        "DAuth user info fetch failed. status={}, responseBody={}",
                        clientResponse.statusCode(),
                        body
                    )
                    Mono.error(CustomException(UserError.USER_NOT_FOUND))
                }
            }
            .bodyToMono(DAuthUser::class.java)
            .block() ?: throw CustomException(UserError.USER_NOT_FOUND)

        return response
    }
}
