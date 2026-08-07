package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthTokenResponse
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.domain.auth.infrastructure.DAuthUserMeResponse
import com.b.beep.domain.auth.infrastructure.DodamConsentRequest
import com.b.beep.domain.auth.infrastructure.DodamConsentResponse
import com.b.beep.domain.auth.infrastructure.DodamMiniAppProperties
import com.b.beep.global.exception.CustomException
import io.netty.channel.ChannelOption
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component
class DodamMiniAppOAuthClient(
    private val properties: DodamMiniAppProperties,
) {
    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                    .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
            )
        )
        .build()

    fun authorize(dodamToken: String, state: String) {
        execute {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/oauth/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", properties.clientId)
                        .queryParam("redirect_uri", properties.redirectUri)
                        .queryParam("scope", properties.scope)
                        .queryParam("state", state)
                        .build()
                }
                .headers { it.setBearerAuth(dodamToken) }
                .retrieve()
                .mapDodamTokenErrors()
                .toBodilessEntity()
        }
    }

    fun consent(dodamToken: String, state: String): String {
        val response = execute {
            webClient.post()
                .uri("/oauth/authorize/consent")
                .headers { it.setBearerAuth(dodamToken) }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    DodamConsentRequest(
                        clientId = properties.clientId,
                        redirectUri = properties.redirectUri,
                        scope = properties.scope,
                        state = state,
                        approved = true
                    )
                )
                .retrieve()
                .mapDodamTokenErrors()
                .bodyToMono(DodamConsentResponse::class.java)
        }

        return response.data?.redirectUri
            ?.takeIf { it.isNotBlank() }
            ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
    }

    fun exchangeCode(code: String): String {
        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("redirect_uri", properties.redirectUri)
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
        }

        val response = execute {
            webClient.post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .mapServerErrors()
                .bodyToMono(DAuthTokenResponse::class.java)
        }

        return response.accessToken.takeIf { it.isNotBlank() }
            ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
    }

    fun getUser(oAuthAccessToken: String): DAuthUser {
        val response = execute {
            webClient.get()
                .uri("/user/me")
                .headers { it.setBearerAuth(oAuthAccessToken) }
                .retrieve()
                .mapServerErrors()
                .bodyToMono(DAuthUserMeResponse::class.java)
        }

        return response.data ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
    }

    private fun WebClient.ResponseSpec.mapDodamTokenErrors(): WebClient.ResponseSpec =
        onStatus(
            { it == HttpStatus.UNAUTHORIZED || it == HttpStatus.FORBIDDEN },
            { Mono.error(CustomException(AuthError.INVALID_DODAM_TOKEN)) }
        ).mapServerErrors()

    private fun WebClient.ResponseSpec.mapServerErrors(): WebClient.ResponseSpec =
        onStatus(
            { !it.is2xxSuccessful },
            { Mono.error(CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)) }
        )

    private fun <T : Any> execute(request: () -> Mono<T>): T {
        return try {
            request().block() ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        } catch (exception: CustomException) {
            throw exception
        } catch (exception: Exception) {
            throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 3_000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
    }
}
