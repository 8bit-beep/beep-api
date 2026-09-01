package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.response.OutSleepingStudentResponse
import com.b.beep.domain.absence.repository.OutSleepingQueryRepository
import com.b.beep.domain.absence.repository.OutSleepingStudentQueryResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OutSleepingOpenApiServiceTest {
    @Mock
    private lateinit var repository: OutSleepingQueryRepository

    @InjectMocks
    private lateinit var service: OutSleepingOpenApiService

    @Test
    fun `조회 결과를 외부 응답 형식으로 변환한다`() {
        val date = LocalDate.of(2026, 5, 20)
        whenever(repository.findAllStudents(date)).thenReturn(
            listOf(OutSleepingStudentQueryResult(null, "홍길동", 2, 3, 15))
        )

        val response = service.search(date)

        assertThat(response.content).containsExactly(
            OutSleepingStudentResponse(null, "홍길동", 2, 3, 15)
        )
    }
}
