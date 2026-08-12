package com.b.beep.domain.auth.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dodam.mini-app")
data class DodamMiniAppProperties(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
    val baseUrl: String = "https://dodam-api.b1nd.com",
    val scope: String = "profile:read notification:write",
)
