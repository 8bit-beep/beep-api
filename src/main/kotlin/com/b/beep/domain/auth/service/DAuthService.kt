package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.controller.dto.request.LoginRequest
import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthProperties
import com.b.beep.domain.auth.infrastructure.DAuthTokenResponse
import com.b.beep.domain.auth.infrastructure.DAuthUserResponse
import com.b.beep.domain.user.domain.UserRole
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class DAuthService(
    private val dAuthProperties: DAuthProperties,
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
) {
    fun login(request: LoginRequest): TokenResponse {
        val token = getDAuthToken(request.code)
        val dodamUser = getDAuthUser(token)
        val user = studentInfoService.getOrCreateUser(dodamUser)

        if (user.role == UserRole.STUDENT) {
            studentInfoService.getOrCreateStudentInfo(user, dodamUser)
        }

        return jwtProvider.generateToken(user.email)
    }

    private fun getDAuthToken(code: String): String {
        val webClient: WebClient = WebClient.create("https://dauthapi.b1nd.com")

        val clientId = dAuthProperties.clientId
        val clientSecret = dAuthProperties.clientSecret

        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters
                    .fromFormData("code", code)
                    .with("grant_type", "authorization_code")
                    .with("redirect_uri", "https://beepapi.com/login/oauth2/code/dauth")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
            )
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .flatMap { body -> Mono.error(CustomException(AuthError.TOKEN_FETCH_FAILED)) }
            }
            .bodyToMono(DAuthTokenResponse::class.java)
            .block()

        return response?.accessToken ?: throw CustomException(AuthError.TOKEN_FETCH_FAILED)
    }

    private fun getDAuthUser(token: String): DAuthUserResponse {
        val webClient: WebClient = WebClient.create("https://dauthapi.b1nd.com")

        val response = webClient.get()
            .uri("/oauth/userinfo")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .doOnNext { body ->
                        println(body)
                    }
                    .flatMap { body ->
                        Mono.error(RuntimeException("Failed to fetch token: $body"))
                    }
            }
            .bodyToMono(DAuthUserResponse::class.java)
            .block() ?: throw CustomException(UserError.USER_NOT_FOUND)

        return response
    }
}
