package com.b.beep.domain.absence.controller

import com.b.beep.domain.absence.controller.dto.response.OutSleepingContentResponse
import com.b.beep.domain.absence.controller.dto.response.OutSleepingResponse
import com.b.beep.domain.absence.controller.dto.response.OutSleepingStudentResponse
import com.b.beep.domain.absence.service.OutSleepingOpenApiService
import com.b.beep.domain.auth.service.CustomOAuth2UserService
import com.b.beep.domain.auth.service.OAuth2FailureHandler
import com.b.beep.domain.auth.service.OAuth2SuccessHandler
import com.b.beep.global.config.WebConfig
import com.b.beep.global.security.SecurityConfig
import com.b.beep.global.security.apikey.QvikApiKeyInterceptor
import com.b.beep.global.security.apikey.QvikOpenApiProperties
import com.b.beep.global.security.jwt.JwtExtractor
import com.b.beep.global.security.jwt.filter.JwtAuthenticationFilter
import com.b.beep.global.security.jwt.filter.JwtExceptionFilter
import com.b.beep.global.security.jwt.handler.JwtAccessDeniedHandler
import com.b.beep.global.security.jwt.handler.JwtAuthenticationEntryPoint
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@WebMvcTest(
    controllers = [OutSleepingOpenApiController::class],
    properties = [
        "qvik.openapi.api-key=test-qvik-api-key",
        "spring.upload.dir=/tmp/test-uploads",
    ],
)
@EnableConfigurationProperties(QvikOpenApiProperties::class)
@Import(
    SecurityConfig::class,
    WebConfig::class,
    QvikApiKeyInterceptor::class,
    JwtAuthenticationFilter::class,
    JwtExceptionFilter::class,
    JwtAccessDeniedHandler::class,
    JwtAuthenticationEntryPoint::class,
)
class OutSleepingOpenApiControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var service: OutSleepingOpenApiService

    @MockBean
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    private lateinit var jwtExtractor: JwtExtractor

    @MockBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockBean
    private lateinit var oAuth2SuccessHandler: OAuth2SuccessHandler

    @MockBean
    private lateinit var oAuth2FailureHandler: OAuth2FailureHandler

    @MockBean(name = "jpaMappingContext")
    private lateinit var jpaMappingContext: JpaMetamodelMappingContext

    @Test
    fun `유효한 API 키로 외박자 명단을 조회한다`() {
        val date = LocalDate.of(2026, 5, 20)
        whenever(service.search(date)).thenReturn(
            OutSleepingResponse(
                listOf(
                    OutSleepingContentResponse(
                        publicId = null,
                        reason = "일반 외박",
                        student = OutSleepingStudentResponse("홍길동", 2, 3, 15),
                        startAt = date,
                        endAt = date,
                    )
                )
            )
        )

        mockMvc.get(QvikApiKeyInterceptor.PATH) {
            param("date", "2026-05-20")
            header(QvikApiKeyInterceptor.HEADER_NAME, "test-qvik-api-key")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(1) }
            jsonPath("$.content[0].publicId") { value(null) }
            jsonPath("$.content[0].reason") { value("일반 외박") }
            jsonPath("$.content[0].student.name") { value("홍길동") }
            jsonPath("$.content[0].student.grade") { value(2) }
            jsonPath("$.content[0].student.room") { value(3) }
            jsonPath("$.content[0].student.number") { value(15) }
            jsonPath("$.content[0].startAt") { value("2026-05-20") }
            jsonPath("$.content[0].endAt") { value("2026-05-20") }
        }
    }

    @Test
    fun `API 키가 없으면 조회를 거부한다`() {
        mockMvc.get(QvikApiKeyInterceptor.PATH) {
            param("date", "2026-05-20")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code") { value("INVALID_QVIK_API_KEY") }
        }
    }

    @Test
    fun `API 키가 다르면 조회를 거부한다`() {
        mockMvc.get(QvikApiKeyInterceptor.PATH) {
            param("date", "2026-05-20")
            header(QvikApiKeyInterceptor.HEADER_NAME, "invalid-key")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code") { value("INVALID_QVIK_API_KEY") }
        }
    }

    @Test
    fun `날짜가 없으면 잘못된 요청으로 응답한다`() {
        mockMvc.get(QvikApiKeyInterceptor.PATH) {
            header(QvikApiKeyInterceptor.HEADER_NAME, "test-qvik-api-key")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("DATE_REQUIRED") }
        }
    }

    @Test
    fun `날짜 형식이 잘못되면 잘못된 요청으로 응답한다`() {
        mockMvc.get(QvikApiKeyInterceptor.PATH) {
            param("date", "2026/05/20")
            header(QvikApiKeyInterceptor.HEADER_NAME, "test-qvik-api-key")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("METHOD_ARGUMENT_TYPE_MISMATCH") }
        }
    }
}
