package com.b.beep.domain.event.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("행사 메모 블록 조립")
class EventMemoComposerTest {

    private val composer = EventMemoComposer()
    private val date = LocalDate.of(2026, 8, 26)

    @Test
    @DisplayName("행사가 없으면 빈 문자열을 만들어 메모에 아무것도 남기지 않는다")
    fun returnsEmptyWhenNoEvents() {
        assertEquals("", composer.compose(date, emptyList()))
    }

    @Test
    @DisplayName("날짜 머리줄 아래에 행사 줄과 학번+이름 명단 줄을 만든다")
    fun composesSingleEvent() {
        val block = composer.compose(
            date,
            listOf(
                EventLine(
                    checkpointNames = listOf("8~9교시"),
                    eventName = "체육대회",
                    teacherName = "천준범",
                    students = listOf("1101 김철수", "1102 이영희", "1103 박민수")
                )
            )
        )

        assertEquals(
            """
            8월 26일
            8~9교시 체육대회 (3명 참여) - 천준범
            1101 김철수 / 1102 이영희 / 1103 박민수
            """.trimIndent(),
            block
        )
    }

    @Test
    @DisplayName("한 행사가 여러 교시에 걸치면 교시를 콤마로 이어 붙인다")
    fun joinsMultipleCheckpointNames() {
        val block = composer.compose(
            date,
            listOf(
                EventLine(
                    checkpointNames = listOf("8~9교시", "10~11교시"),
                    eventName = "체육대회",
                    teacherName = "천준범",
                    students = listOf("1101 김철수")
                )
            )
        )

        assertTrue(
            block.contains("8~9교시, 10~11교시 체육대회 (1명 참여) - 천준범"),
            "실제 블록: $block"
        )
    }

    @Test
    @DisplayName("행사가 여러 건이어도 날짜 머리줄은 한 번만 넣는다")
    fun writesDateHeaderOnce() {
        val block = composer.compose(
            date,
            listOf(
                EventLine(listOf("8~9교시"), "체육대회", "천준범", listOf("1101 김철수")),
                EventLine(listOf("10~11교시"), "동아리발표", "김지영", listOf("1104 최수연"))
            )
        )

        assertEquals(
            """
            8월 26일
            8~9교시 체육대회 (1명 참여) - 천준범
            1101 김철수
            10~11교시 동아리발표 (1명 참여) - 김지영
            1104 최수연
            """.trimIndent(),
            block
        )
    }
}
