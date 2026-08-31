package com.b.beep.global.config

import com.b.beep.global.security.apikey.QvikApiKeyInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${spring.upload.dir}") private val uploadDir: String,
    private val qvikApiKeyInterceptor: QvikApiKeyInterceptor,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:${uploadDir}/")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(qvikApiKeyInterceptor)
            .addPathPatterns(QvikApiKeyInterceptor.PATH)
    }
}
