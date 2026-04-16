package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthProperties
import com.b.beep.domain.auth.infrastructure.DAuthTokenResponse
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.infrastructure.DAuthUserMeResponse
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Service
class DAuthService(
    private val dAuthProperties: DAuthProperties,
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
) {
    private val mapper = jacksonObjectMapper()

    fun login(request: LoginRequest): TokenResponse {
            val token = getDAuthToken(request.code, request.codeVerifier)
            val dodamUser = getDAuthUser(token)
            val user = studentInfoService.getOrCreateUser(dodamUser)
            if (user.role == UserRole.STUDENT) {
                studentInfoService.getOrCreateStudentInfo(user, dodamUser)
                studentInfoService.updateStudentInfo(user, dodamUser)
            }
            return jwtProvider.generateToken(user.username)
    }

    private fun getDAuthToken(code: String, codeVerifier: String): String {
        val webClient: WebClient = WebClient.create("https://dodam-api.b1nd.com")

        val clientId = dAuthProperties.clientId
        val clientSecret = dAuthProperties.clientSecret

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw CustomException(AuthError.TOKEN_FETCH_FAILED)
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("grant_type", "authorization_code")
            add("redirect_uri", "https://beep.cher1shrxd.me/callback/dauth")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code_verifier", codeVerifier)
        }
        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus({ it.isError }) {
                Mono.error(CustomException(AuthError.TOKEN_FETCH_FAILED))
            }
            .bodyToMono(DAuthTokenResponse::class.java)
            .block()

        return response?.accessToken ?: throw CustomException(AuthError.TOKEN_FETCH_FAILED)
    }

    private fun getDAuthUser(token: String): DAuthUser {
        val webClient: WebClient = WebClient.create("https://dodam-api.b1nd.com")
        val raw = webClient.get()
            .uri("/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.is4xxClientError }) { cr ->
                cr.bodyToMono(String::class.java).flatMap {
                    Mono.error(CustomException(AuthError.INVALID_DAUTH_TOKEN))
                }
            }
            .onStatus({ it.is5xxServerError }) { cr ->
                cr.bodyToMono(String::class.java).flatMap {
                    Mono.error(CustomException(AuthError.DAUTH_SERVER_ERROR))
                }
            }
            .bodyToMono(String::class.java)
            .block() ?: throw CustomException(AuthError.INVALID_DAUTH_TOKEN)

        val wrapper: DAuthUserMeResponse = mapper.readValue(raw)
        return wrapper.data ?: throw CustomException(AuthError.INVALID_DAUTH_TOKEN)
    }
}
