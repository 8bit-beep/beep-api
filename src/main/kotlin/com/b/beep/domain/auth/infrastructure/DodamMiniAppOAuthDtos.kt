package com.b.beep.domain.auth.infrastructure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class DodamConsentRequest(
    val clientId: String,
    val redirectUri: String,
    val scope: String,
    val state: String,
    val approved: Boolean,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DodamConsentResponse(
    val data: DodamConsentData? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DodamConsentData(
    val redirectUri: String? = null,
)
