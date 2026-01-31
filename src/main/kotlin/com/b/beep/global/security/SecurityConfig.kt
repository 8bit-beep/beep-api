package com.b.beep.global.security

import com.b.beep.domain.auth.service.CustomOAuth2UserService
import com.b.beep.domain.auth.service.OAuth2SuccessHandler
import com.b.beep.global.security.jwt.filter.JwtAuthenticationFilter
import com.b.beep.global.security.jwt.filter.JwtExceptionFilter
import com.b.beep.global.security.jwt.handler.JwtAccessDeniedHandler
import com.b.beep.global.security.jwt.handler.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionFilter: JwtExceptionFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .rememberMe { it.disable() }
        .logout { it.disable() }

        .exceptionHandling {
            it.accessDeniedHandler(jwtAccessDeniedHandler)
            it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        }

        .authorizeHttpRequests {
            it
                .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**", "/api-docs").permitAll()
                .requestMatchers("/auth/**", "/dauth/login", "/test/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/users/my").authenticated()
                .requestMatchers("/fcm/**").authenticated()

                // STUDENT
                .requestMatchers(HttpMethod.POST, "/attendances").hasRole("STUDENT")
                .requestMatchers(HttpMethod.PATCH, "/attendances/cancel").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/shifts").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/shifts/my").hasRole("STUDENT")
                .requestMatchers(HttpMethod.PATCH, "/shifts/{id}").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/shifts/{id}").hasRole("STUDENT")
                .requestMatchers("/schedules/my", "/schedules/my/**").hasRole("STUDENT")

                // TEACHER
                .requestMatchers(HttpMethod.GET, "/attendances/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/attendances/**").hasRole("TEACHER")
                .requestMatchers("/absences/**").hasRole("TEACHER")
                .requestMatchers("/approvals/**").hasRole("TEACHER")
                .requestMatchers("/memos/**").hasRole("TEACHER")
                .requestMatchers("/schedules/**").hasRole("TEACHER")
                .requestMatchers("/students/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/shifts").hasAnyRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/shifts/{id}/status").hasAnyRole("TEACHER")
                .requestMatchers("/attendances/histories/**").hasAnyRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/rooms/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/rooms").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/rooms/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/limited-users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                .requestMatchers("/checkpoints/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/types/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/types").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/types/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/types/**").hasAnyRole("TEACHER", "ADMIN")

                .anyRequest().authenticated()
        }
        .oauth2Login { oauth2 ->
            oauth2
                .userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                .successHandler(oAuth2SuccessHandler)
        }
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .addFilterBefore(jwtExceptionFilter, jwtAuthenticationFilter::class.java)

        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", CorsConfiguration().apply {
            allowedOriginPatterns = listOf("http://localhost:5173", "http://localhost:8085", "https://beep.cher1shrxd.me", "https://dev-beep.cher1shrxd.me","https://teacher.8beep.site","http://localhost:3000","https://admin.8beep.site")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        })
    }


}