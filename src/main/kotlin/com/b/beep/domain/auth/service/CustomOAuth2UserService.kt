package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.domain.AuthError
import com.b.beep.domain.auth.infrastructure.DAuthUser
import com.b.beep.global.exception.CustomException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)

        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes = oauth2User.attributes

        val email = attributes["email"] as? String
            ?: throw CustomException(AuthError.NULL_EMAIL)
        val name = attributes["name"] as? String
            ?:throw CustomException(AuthError.NULL_NAME)
        val grade = (attributes["grade"] as? Number)?.toInt()
            ?:throw CustomException(AuthError.NULL_STU_NUM)
        val room = (attributes["room"] as? Number)?.toInt()
            ?:throw CustomException(AuthError.NULL_STU_NUM)
        val number = (attributes["number"] as? Number)?.toInt()
            ?:throw CustomException(AuthError.NULL_STU_NUM)

        val dauthUser = DAuthUser(
            id = attributes["sub"] as String,
            name = name,
            email = email,
            profileImage = attributes["profile_image"] as? String,
            role = attributes["role"] as? String,
            phone = attributes["phone"] as? String,
            grade = grade,
            room = room,
            number = number
        )

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            userNameAttributeName
        )
    }
}
