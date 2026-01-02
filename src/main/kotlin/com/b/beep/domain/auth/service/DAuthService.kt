package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.domain.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthProperties
import com.b.beep.domain.auth.infrastructure.DAuthTokenResponse
import com.b.beep.domain.auth.infrastructure.DAuthUserResponse
import com.b.beep.domain.auth.infrastructure.GetDAuthTokenRequest
import com.b.beep.domain.user.domain.UserError
import com.b.beep.global.exception.CustomException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class DAuthService(
    private val dAuthProperties: DAuthProperties
) {
    fun getDAuthToken(code: String): String {
        val webClient: WebClient = WebClient.create("https://dauthapi.b1nd.com")

        val clientId = dAuthProperties.clientId

        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters
                    .fromFormData("code", code)
                    .with("redirect_uri", "https://beepapi.com/login/oauth2/code/dauth")
                    .with("client_id", clientId)
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

    fun getDAuthUser(token: String): DAuthUserResponse {
        val webClient: WebClient = WebClient.create("https://opendodam.b1nd.com")

        val response = webClient.get()
            .uri("/api/user")
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
