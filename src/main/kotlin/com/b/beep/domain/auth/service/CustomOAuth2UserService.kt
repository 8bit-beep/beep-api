package com.b.beep.domain.auth.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val token = userRequest.accessToken.tokenValue
        val uri = userRequest.clientRegistration.providerDetails.userInfoEndpoint.uri

        val restTemplate = org.springframework.web.client.RestTemplate()
        val headers = org.springframework.http.HttpHeaders()
        headers.setBearerAuth(token)
        val entity = org.springframework.http.HttpEntity<String>(headers)
        val response = restTemplate.exchange(uri, org.springframework.http.HttpMethod.GET, entity, Map::class.java)

        val body = response.body as Map<*, *>
        val data = body["data"] as Map<*, *>

        @Suppress("UNCHECKED_CAST")
        val attributes = data as Map<String, Any>

        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            userNameAttributeName
        )
    }
}
