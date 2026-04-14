package com.b.beep.domain.auth.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty

data class DAuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("id_token")
    val idToken: String? = null,
    @JsonProperty("token_type")
    val tokenType: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Long? = null,
    @JsonProperty("scope")
    val scope: String? = null,
)
