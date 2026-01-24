package com.b.beep.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application.domain")
class DomainProperties {
    lateinit var web: String
}