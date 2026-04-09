package com.b.beep.domain.auth.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class CustomOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private val log = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)

        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes = oauth2User.attributes
        log.info("DAuth attributes: $attributes")

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            userNameAttributeName
        )
    }
}
