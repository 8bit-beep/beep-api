package com.b.beep.domain.event.domain

import com.b.beep.domain.event.domain.entity.EventCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.event.domain.entity.EventUserEntity
import com.b.beep.domain.event.repository.EventCheckpointRepository
import com.b.beep.domain.event.repository.EventRepository
import com.b.beep.domain.event.repository.EventUserRepository
import com.b.beep.domain.memo.service.MemoService
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.repository.StudentInfoRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 해당 날짜의 행사 전체를 다시 읽어 학년별 메모 블록을 통째로 갈아끼운다.
 * 교사가 쓴 수기 영역은 MemoService가 지켜준다.
 */
@Component
class EventMemoWriter(
    private val eventRepository: EventRepository,
    private val eventUserRepository: EventUserRepository,
    private val eventCheckpointRepository: EventCheckpointRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val memoService: MemoService,
    private val eventMemoComposer: EventMemoComposer
) {
    fun refresh(date: LocalDate, grades: Set<Int>) {
        if (grades.isEmpty()) return

        val events = eventRepository.findAllByDateOrderByIdAsc(date)
        val eventIds = events.mapNotNull { it.id }

        val usersByEventId = if (eventIds.isEmpty()) {
            emptyMap()
        } else {
            eventUserRepository.findAllByEventIdIn(eventIds).groupBy { it.event.id }
        }
        val checkpointsByEventId = if (eventIds.isEmpty()) {
            emptyMap()
        } else {
            eventCheckpointRepository.findAllByEventIdIn(eventIds).groupBy { it.event.id }
        }

        val participants = usersByEventId.values.flatten().map { it.user }.distinctBy { it.id }
        val infoByUserId = if (participants.isEmpty()) {
            emptyMap()
        } else {
            studentInfoRepository.findAllByUserIn(participants).associateBy { it.user.id }
        }

        grades.forEach { grade ->
            val lines = events.mapNotNull { event ->
                toLine(
                    event = event,
                    grade = grade,
                    eventUsers = usersByEventId[event.id].orEmpty(),
                    eventCheckpoints = checkpointsByEventId[event.id].orEmpty(),
                    infoByUserId = infoByUserId
                )
            }
            memoService.replaceEventBlock(grade, eventMemoComposer.compose(date, lines))
        }
    }

    /** 그 학년 참여자가 없는 행사는 해당 학년 블록에서 빠진다. */
    private fun toLine(
        event: EventEntity,
        grade: Int,
        eventUsers: List<EventUserEntity>,
        eventCheckpoints: List<EventCheckpointEntity>,
        infoByUserId: Map<Long?, StudentInfoEntity>
    ): EventLine? {
        val students = eventUsers
            .mapNotNull { infoByUserId[it.user.id] }
            .filter { it.grade == grade }
            .sortedWith(compareBy({ it.classNumber }, { it.num }))
            .map { "${studentNumber(it)} ${it.user.name}" }

        if (students.isEmpty()) return null

        return EventLine(
            checkpointNames = eventCheckpoints.sortedBy { it.checkpoint.startAt }.map { it.checkpoint.name },
            eventName = event.name,
            teacherName = event.createdBy.name,
            students = students
        )
    }

    private fun studentNumber(info: StudentInfoEntity): String =
        String.format("%d%d%02d", info.grade, info.classNumber, info.num)
}
