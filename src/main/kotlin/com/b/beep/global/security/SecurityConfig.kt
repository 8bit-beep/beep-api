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
                .requestMatchers(HttpMethod.POST, "/auth/sign-in", "/auth/sign-up", "/auth/refresh").permitAll()

                .requestMatchers(HttpMethod.POST, "/email/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/email/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/users").authenticated()
                .requestMatchers(HttpMethod.POST, "/users/password/send").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/users/password/**").permitAll()

                // student
                .requestMatchers(HttpMethod.POST, "/shifts").hasAnyRole("STUDENT", "TEACHER")
                .requestMatchers(HttpMethod.POST, "/attends").hasAnyRole("STUDENT", "TEACHER")

                // teacher
                .requestMatchers(HttpMethod.GET, "/shifts").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/shifts/**").hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/attends/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/rooms").hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/not-attends").hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/long-absences").hasRole("TEACHER")
                .requestMatchers(HttpMethod.POST, "/long-absences").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/long-absences/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/long-absences/**").hasRole("TEACHER")

//                .requestMatchers(HttpMethod.GET, "/excel/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.POST, "/excel/**").hasRole("TEACHER")

                .requestMatchers(HttpMethod.POST, "/approve").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/approve/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.GET, "/approve").hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/students/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/students/**").hasRole("TEACHER")

                .requestMatchers(HttpMethod.GET, "/memo").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PATCH, "/memo").hasRole("TEACHER")

                .anyRequest().permitAll()
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
            allowedOriginPatterns = listOf("http://localhost:5173", "https://beep.cher1shrxd.me", "https://dev-beep.cher1shrxd.me")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        })
    }


}