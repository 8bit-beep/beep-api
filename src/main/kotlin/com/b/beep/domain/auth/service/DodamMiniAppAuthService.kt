package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.error.AuthError
import com.b.beep.domain.auth.infrastructure.DodamMiniAppProperties
import com.b.beep.domain.user.service.StudentInfoService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.jwt.JwtProvider
import com.b.beep.global.security.jwt.dto.response.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

@Service
class DodamMiniAppAuthService(
    private val oAuthClient: DodamMiniAppOAuthClient,
    private val properties: DodamMiniAppProperties,
    private val studentInfoService: StudentInfoService,
    private val jwtProvider: JwtProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom()

    fun login(dodamToken: String): TokenResponse {
        validateConfiguration()

        val state = generateState()
        oAuthClient.authorize(dodamToken, state)

        val consentRedirectUri = oAuthClient.consent(dodamToken, state)
        val authorizationCode = extractAuthorizationCode(consentRedirectUri, state)
        val oAuthAccessToken = oAuthClient.exchangeCode(authorizationCode)
        val dodamUser = oAuthClient.getUser(oAuthAccessToken)
        val user = studentInfoService.upsertFromDodamMiniApp(dodamUser)

        return jwtProvider.generateToken(user.username)
    }

    private fun validateConfiguration() {
        val clientIdConfigured = properties.clientId.isNotBlank()
        val clientSecretConfigured = properties.clientSecret.isNotBlank()
        val redirectUriConfigured = properties.redirectUri.isNotBlank()

        if (!clientIdConfigured || !clientSecretConfigured || !redirectUriConfigured) {
            logger.error(
                "Dodam mini-app OAuth configuration is incomplete: " +
                    "clientIdConfigured={}, clientSecretConfigured={}, redirectUriConfigured={}",
                clientIdConfigured,
                clientSecretConfigured,
                redirectUriConfigured
            )
            throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        }
    }

    private fun generateState(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun extractAuthorizationCode(redirectUri: String, expectedState: String): String {
        val configuredUri = parseUri(properties.redirectUri)
        val returnedUri = parseUri(redirectUri)

        if (!hasSameRedirectTarget(configuredUri, returnedUri)) {
            throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        }

        val queryParameters = UriComponentsBuilder.fromUri(returnedUri).build().queryParams
        if (queryParameters.getFirst("state") != expectedState) {
            throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        }

        return queryParameters.getFirst("code")
            ?.takeIf { it.isNotBlank() }
            ?: throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
    }

    private fun parseUri(value: String): URI {
        return try {
            URI(value)
        } catch (exception: Exception) {
            throw CustomException(AuthError.DODAM_OAUTH_SERVER_ERROR)
        }
    }

    private fun hasSameRedirectTarget(configuredUri: URI, returnedUri: URI): Boolean {
        val configuredHost = configuredUri.host ?: return false
        val returnedHost = returnedUri.host ?: return false

        return configuredUri.scheme.equals("https", ignoreCase = true) &&
            configuredUri.scheme.equals(returnedUri.scheme, ignoreCase = true) &&
            configuredHost.equals(returnedHost, ignoreCase = true) &&
            effectivePort(configuredUri) == effectivePort(returnedUri) &&
            configuredUri.path == returnedUri.path
    }

    private fun effectivePort(uri: URI): Int {
        if (uri.port != -1) {
            return uri.port
        }

        return when (uri.scheme.lowercase()) {
            "https" -> 443
            "http" -> 80
            else -> -1
        }
    }
}
