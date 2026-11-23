package com.b.beep.global.security

import com.b.beep.domain.attendance.controller.AttendanceSortModeController
import com.b.beep.domain.attendance.controller.BeaconAttendanceController
import com.b.beep.domain.attendance.controller.StudentAttendanceController
import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.controller.dto.request.ScanHelpQrRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceSortModeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceSortModesResponse
import com.b.beep.domain.attendance.service.AttendanceHelpService
import com.b.beep.domain.attendance.service.AttendanceSortModeService
import com.b.beep.domain.attendance.service.StudentAttendanceService
import com.b.beep.domain.auth.service.CustomOAuth2UserService
import com.b.beep.domain.event.controller.EventController
import com.b.beep.domain.event.service.EventService
import com.b.beep.domain.auth.service.OAuth2FailureHandler
import com.b.beep.domain.auth.service.OAuth2SuccessHandler
import com.b.beep.domain.room.controller.RoomController
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.user.controller.StudentController
import com.b.beep.domain.user.service.StudentActivityRoomService
import com.b.beep.domain.user.service.StudentService
import com.b.beep.global.security.apikey.QvikOpenApiProperties
import com.b.beep.global.security.jwt.JwtExtractor
import com.b.beep.global.security.jwt.filter.JwtAuthenticationFilter
import com.b.beep.global.security.jwt.filter.JwtExceptionFilter
import com.b.beep.global.security.jwt.handler.JwtAccessDeniedHandler
import com.b.beep.global.security.jwt.handler.JwtAuthenticationEntryPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate

@WebMvcTest(
    controllers = [
        StudentController::class,
        AttendanceSortModeController::class,
        EventController::class,
        BeaconAttendanceController::class,
        StudentAttendanceController::class,
        RoomController::class,
    ]
)
@EnableConfigurationProperties(QvikOpenApiProperties::class)
@Import(
    SecurityConfig::class,
    JwtAuthenticationFilter::class,
    JwtExceptionFilter::class,
    JwtAccessDeniedHandler::class,
    JwtAuthenticationEntryPoint::class
)
class SecurityAuthorizationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var studentService: StudentService

    @MockBean
    private lateinit var studentActivityRoomService: StudentActivityRoomService

    @MockBean
    private lateinit var attendanceSortModeService: AttendanceSortModeService

    @MockBean
    private lateinit var studentAttendanceService: StudentAttendanceService

    @MockBean
    private lateinit var attendanceHelpService: AttendanceHelpService

    @MockBean
    private lateinit var roomService: RoomService

    @MockBean
    private lateinit var eventService: EventService

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

    @ParameterizedTest
    @ValueSource(strings = ["STUDENT", "TEACHER", "ADMIN"])
    fun `실 조회 권한으로 비콘 등록 반을 조회할 수 있다`(role: String) {
        whenever(roomService.getBeaconRooms()).thenReturn(emptyList())

        mockMvc.get("/rooms/beacons") {
            with(user("test-user").roles(role))
        }.andExpect { status { isOk() } }

        verify(roomService).getBeaconRooms()
    }

    @Test
    fun `미인증 사용자는 비콘 등록 반을 조회할 수 없다`() {
        mockMvc.get("/rooms/beacons")
            .andExpect { status { isUnauthorized() } }

        verifyNoInteractions(roomService)
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 비콘 출석을 요청할 수 있다`() {
        mockMvc.post("/attendances/beacon") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"roomId":3,"typeId":1}"""
        }.andExpect {
            status { isCreated() }
            content { string("") }
        }

        verify(studentAttendanceService).attend(CreateAttendanceRequest(3, 1))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            """{"typeId":1}""",
            """{"roomId":3}""",
            """{"roomId":0,"typeId":1}""",
            """{"roomId":3,"typeId":-1}""",
        ]
    )
    @WithMockUser(roles = ["STUDENT"])
    fun `비콘 출석에 누락되거나 양수가 아닌 ID를 사용할 수 없다`(requestBody: String) {
        mockMvc.post("/attendances/beacon") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
        }.andExpect { status { isBadRequest() } }

        verifyNoInteractions(studentAttendanceService)
    }

    @Test
    fun `미인증 사용자는 비콘 출석을 요청할 수 없다`() {
        mockMvc.post("/attendances/beacon") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"roomId":3,"typeId":1}"""
        }.andExpect { status { isUnauthorized() } }

        verifyNoInteractions(studentAttendanceService)
    }

    @ParameterizedTest
    @ValueSource(strings = ["TEACHER", "ADMIN"])
    fun `교사와 관리자는 비콘 출석을 요청할 수 없다`(role: String) {
        mockMvc.post("/attendances/beacon") {
            with(user("test-user").roles(role))
            contentType = MediaType.APPLICATION_JSON
            content = """{"roomId":3,"typeId":1}"""
        }.andExpect { status { isForbidden() } }

        verifyNoInteractions(studentAttendanceService)
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 일반 출석과 QR 출석 및 출석 취소 경로를 사용할 수 있다`() {
        mockMvc.post("/attendances") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"roomId":3,"typeId":1}"""
        }.andExpect { status { isCreated() } }
        mockMvc.post("/attendances/help/scan") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"qr-token","typeId":1}"""
        }.andExpect { status { isCreated() } }
        mockMvc.patch("/attendances/cancel")
            .andExpect { status { isNoContent() } }

        verify(studentAttendanceService).attend(CreateAttendanceRequest(3, 1))
        verify(attendanceHelpService).scanHelpQr(ScanHelpQrRequest("qr-token", 1))
        verify(studentAttendanceService).cancelAttendance()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 활동실을 조회하고 저장할 수 있다`() {
        whenever(studentActivityRoomService.getActivityRooms(1L)).thenReturn(emptyList())
        whenever(studentActivityRoomService.replaceActivityRooms(1L, emptyList())).thenReturn(emptyList())

        mockMvc.get("/students/1/activity-rooms")
            .andExpect { status { isOk() } }
        mockMvc.put("/students/1/activity-rooms") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = "[]"
        }.andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser(roles = ["TEACHER"])
    fun `교사는 활동실을 조회하거나 저장할 수 없다`() {
        mockMvc.get("/students/1/activity-rooms")
            .andExpect { status { isForbidden() } }
        mockMvc.put("/students/1/activity-rooms") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = "[]"
        }.andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["TEACHER"])
    fun `교사는 재정렬 모드를 조회하고 변경할 수 있다`() {
        val response = AttendanceSortModesResponse(LocalDate.of(2026, 8, 3), emptyList())
        whenever(attendanceSortModeService.getSortModes()).thenReturn(response)
        whenever(attendanceSortModeService.updateSortMode(any<UpdateAttendanceSortModeRequest>()))
            .thenReturn(response)

        mockMvc.get("/attendance-sort-modes")
            .andExpect { status { isOk() } }
        mockMvc.patch("/attendance-sort-modes") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"grade":1,"typeId":null}"""
        }.andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 재정렬 모드를 조회하거나 변경할 수 없다`() {
        mockMvc.get("/attendance-sort-modes")
            .andExpect { status { isForbidden() } }
        mockMvc.patch("/attendance-sort-modes") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"grade":1,"typeId":null}"""
        }.andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 재정렬 모드에 접근할 수 없다`() {
        mockMvc.get("/attendance-sort-modes")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["TEACHER"])
    fun `교사는 행사를 조회하고 삭제할 수 있다`() {
        whenever(eventService.getEvents(null)).thenReturn(emptyList())

        mockMvc.get("/events")
            .andExpect { status { isOk() } }
        mockMvc.delete("/events/1")
            .andExpect { status { isNoContent() } }
    }

    @Test
    @WithMockUser(roles = ["STUDENT"])
    fun `학생은 행사를 조회하거나 삭제할 수 없다`() {
        mockMvc.get("/events")
            .andExpect { status { isForbidden() } }
        mockMvc.delete("/events/1")
            .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `관리자는 행사에 접근할 수 없다`() {
        mockMvc.get("/events")
            .andExpect { status { isForbidden() } }
    }
}
