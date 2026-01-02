package com.b.beep.domain.auth.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty

data class DAuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("expires_in")
    val expiresIn: String,
    val scope: String,
)
