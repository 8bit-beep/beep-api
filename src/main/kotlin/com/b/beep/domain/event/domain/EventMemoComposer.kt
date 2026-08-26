package com.b.beep.domain.event.domain

import org.springframework.stereotype.Component
import java.time.LocalDate

data class EventLine(
    val checkpointNames: List<String>,
    val eventName: String,
    val teacherName: String,
    val students: List<String>
)

/**
 * 메모의 자동 영역(행사 블록) 문자열을 만든다.
 * DB에 의존하지 않는 순수 계산이라 단위 테스트로 전부 덮인다.
 */
@Component
class EventMemoComposer {
    fun compose(date: LocalDate, events: List<EventLine>): String {
        if (events.isEmpty()) return ""

        val header = "${date.monthValue}월 ${date.dayOfMonth}일"
        val body = events.flatMap { event ->
            listOf(
                "${event.checkpointNames.joinToString(CHECKPOINT_SEPARATOR)} ${event.eventName} " +
                    "(${event.students.size}명 참여) - ${event.teacherName}",
                event.students.joinToString(STUDENT_SEPARATOR)
            )
        }

        return (listOf(header) + body).joinToString("\n")
    }

    companion object {
        private const val CHECKPOINT_SEPARATOR = ", "
        private const val STUDENT_SEPARATOR = " / "
    }
}
