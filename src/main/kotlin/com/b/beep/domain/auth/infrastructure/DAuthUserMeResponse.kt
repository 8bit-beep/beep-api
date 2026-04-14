package com.b.beep.domain.auth.infrastructure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class DAuthUserMeResponse (
    val status: Int? = null,
    val message: String? = null,
    val data: DAuthUser? = null
)