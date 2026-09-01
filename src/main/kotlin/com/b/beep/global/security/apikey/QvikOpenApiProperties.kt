package com.b.beep.global.security.apikey

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "qvik.openapi")
data class QvikOpenApiProperties(
    @field:NotBlank
    val apiKey: String,
)
