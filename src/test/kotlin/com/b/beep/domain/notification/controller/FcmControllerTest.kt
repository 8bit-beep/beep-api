package com.b.beep.domain.notification.controller

import com.b.beep.domain.notification.controller.dto.request.SaveTokenRequest
import com.b.beep.domain.notification.service.FcmTokenService
import com.b.beep.global.exception.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FcmControllerTest {
    private val tokenService = mock<FcmTokenService>()
    private val mockMvc = MockMvcBuilders.standaloneSetup(FcmController(tokenService))
        .setControllerAdvice(GlobalExceptionHandler())
        .build()
    private val student = UsernamePasswordAuthenticationToken("student", null, emptyList())

    @Test
    fun `rejects GET requests instead of sending a test notification`() {
        mockMvc.get("/fcm") {
            principal = student
        }.andExpect {
            status { isMethodNotAllowed() }
        }

        verifyNoInteractions(tokenService)
    }

    @Test
    fun `continues accepting FCM token registration`() {
        mockMvc.post("/fcm") {
            principal = student
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"test-token","device":"android"}"""
        }.andExpect {
            status { isCreated() }
        }

        verify(tokenService).saveToken(SaveTokenRequest("test-token", "android"))
    }
}
