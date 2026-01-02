package com.b.beep.domain.auth.service

import com.b.beep.domain.auth.infrastructure.DAuthUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CustomOAuth2User(
    val dauthUser: DAuthUser,
    authorities: Collection<GrantedAuthority>,
    attributes: Map<String, Any>,
    nameAttributeKey: String
) : DefaultOAuth2User(authorities, attributes, nameAttributeKey)
