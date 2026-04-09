package com.b.beep.global.security

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames
import java.security.MessageDigest
import java.util.Base64
import jakarta.servlet.http.HttpServletRequest

class CustomAuthorizationRequestResolver (
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizationRequestBaseUri: String
) : OAuth2AuthorizationRequestResolver {
    private val defaultResolver = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, authorizationRequestBaseUri
    )
    private val secureKeyGenerator = Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96)

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request)?.withPkce()

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? =
        defaultResolver.resolve(request, clientRegistrationId)?.withPkce()

    private fun OAuth2AuthorizationRequest.withPkce(): OAuth2AuthorizationRequest {
        val codeVerifier = secureKeyGenerator.generateKey()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        return OAuth2AuthorizationRequest.from(this)
            .additionalParameters(mapOf(
                PkceParameterNames.CODE_CHALLENGE to codeChallenge,
                PkceParameterNames.CODE_CHALLENGE_METHOD to "S256"
            ))
            .attributes(attributes + mapOf(PkceParameterNames.CODE_VERIFIER to codeVerifier))
            .build()
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}