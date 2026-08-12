package com.b.beep.domain.notification.service

import com.b.beep.domain.notification.controller.dto.request.SendNotificationRequest
import com.b.beep.domain.notification.infrastructure.DodamNotificationClient
import com.b.beep.domain.notification.infrastructure.DodamNotificationResult
import com.b.beep.domain.notification.repository.NotificationTargetQueryRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotificationServiceTest {
    private val client = mock<DodamNotificationClient>()
    private val targetRepository = mock<NotificationTargetQueryRepository>()
    private lateinit var service: NotificationService

    @BeforeEach
    fun setUp() {
        service = NotificationService(client, targetRepository)
    }

    @Test
    fun `sends attendance start notification to all eligible students through Dodam`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(listOf("student-1", "student-2"))
        whenever(client.send(any(), any(), any())).thenReturn(DodamNotificationResult(2, 0))

        service.sendToAll(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            imageUrl = "legacy-image",
        )

        verify(client).send(
            title = "출석 시간입니다!",
            body = "저녁 출석이 시작되었습니다.",
            targetUserPublicIds = listOf("student-1", "student-2"),
        )
    }

    @Test
    fun `sends attendance deadline notification only to not attended students`() {
        whenever(targetRepository.findAllNotAttendedPublicIds()).thenReturn(listOf("student-2"))
        whenever(client.send(any(), any(), any())).thenReturn(DodamNotificationResult(1, 0))

        service.sendToNotAttended(
            title = "출석 마감 임박!",
            body = "저녁 출석이 5분 후 마감됩니다.",
            imageUrl = "legacy-image",
        )

        verify(client).send(
            title = "출석 마감 임박!",
            body = "저녁 출석이 5분 후 마감됩니다.",
            targetUserPublicIds = listOf("student-2"),
        )
    }

    @Test
    fun `does not call Dodam API when there are no targets`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(emptyList())

        service.sendPushMessage(SendNotificationRequest("title", "body"), isAll = true)

        verify(client, never()).send(any(), any(), any())
    }

    @Test
    fun `does not propagate Dodam API failures`() {
        whenever(targetRepository.findAllActiveStudentPublicIds()).thenReturn(listOf("student-1"))
        whenever(client.send(any(), any(), any())).thenThrow(IllegalStateException("Dodam unavailable"))

        assertDoesNotThrow {
            service.sendPushMessage(SendNotificationRequest("title", "body"), isAll = true)
        }
    }
}
