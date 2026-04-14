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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters

@Service
class DAuthService(
    private val dAuthProperties: DAuthProperties,
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
) {
    private val log = LoggerFactory.getLogger(DAuthService::class.java)
    private fun maskEdge(value: String, edge: Int = 4): String {
        if (value.length <= edge * 2) return "*".repeat(value.length)
        return value.take(edge) + "..." + value.takeLast(edge)
    }

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

//        val requestBody = mapOf(
//            "code" to code,
//            "grant_type" to "authorization_code",
//            "redirect_uri" to "https://beep.cher1shrxd.me/callback/dauth",
//            "client_id" to dAuthProperties.clientId,
//            "client_secret" to dAuthProperties.clientSecret,
//            "code_verifier" to codeVerifier
//        )
//
//        val response = webClient.post()
//            .uri("/oauth/token")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(requestBody)
//            .retrieve()
//            .onStatus({ it.isError }) { clientResponse ->
//                clientResponse.bodyToMono(String::class.java).flatMap { body ->
//                    log.error("DAuth token exchange failed. status={}, responseBody={}", clientResponse.statusCode(), body)
//                    Mono.error(CustomException(AuthError.TOKEN_FETCH_FAILED))
//                }
//            }
//            .bodyToMono(DAuthTokenResponse::class.java)
//            .block()

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("grant_type", "authorization_code")
            add("redirect_uri", "https://beep.cher1shrxd.me/callback/dauth")
            add("client_id", dAuthProperties.clientId)
            add("client_secret", dAuthProperties.clientSecret)
            add("code_verifier", codeVerifier)
        }

        log.info(
            "DAuth token request check: hasCode={}, codeLen={}, codeEdge={}, hasVerifier={}, verifierLen={}, verifierEdge={}, redirectUri={}, hasClientId={}, clientIdEdge={}, hasClientSecret={}, clientSecretLen={}, clientSecretEdge={}",
            code.isNotBlank(),
            code.length,
            maskEdge(code),
            codeVerifier.isNotBlank(),
            codeVerifier.length,
            maskEdge(codeVerifier),
            formData["redirect_uri"],
            !dAuthProperties.clientId.isNullOrBlank(),
            maskEdge(dAuthProperties.clientId),
            !dAuthProperties.clientSecret.isNullOrBlank(),
            dAuthProperties.clientSecret.length,
            maskEdge(dAuthProperties.clientSecret, 4)
        )

        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus({ it.isError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java).flatMap { body ->
                    log.error("DAuth token exchange failed(FORM). status={}, responseBody={}", clientResponse.statusCode(), body)
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
