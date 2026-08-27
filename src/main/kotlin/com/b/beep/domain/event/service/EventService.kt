package com.b.beep.domain.event.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.event.controller.dto.request.CreateEventRequest
import com.b.beep.domain.event.controller.dto.request.UpdateEventRequest
import com.b.beep.domain.event.controller.dto.response.EventDetailResponse
import com.b.beep.domain.event.controller.dto.response.EventResponse
import com.b.beep.domain.event.controller.dto.response.EventStudentResponse
import com.b.beep.domain.event.domain.EventMemoWriter
import com.b.beep.domain.event.domain.entity.EventCheckpointEntity
import com.b.beep.domain.event.domain.entity.EventEntity
import com.b.beep.domain.event.domain.entity.EventUserEntity
import com.b.beep.domain.event.error.EventError
import com.b.beep.domain.event.repository.EventCheckpointRepository
import com.b.beep.domain.event.repository.EventRepository
import com.b.beep.domain.event.repository.EventUserRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class EventService(
    private val eventRepository: EventRepository,
    private val eventUserRepository: EventUserRepository,
    private val eventCheckpointRepository: EventCheckpointRepository,
    private val userRepository: UserRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val attendanceRepository: AttendanceRepository,
    private val attendanceTypeService: AttendanceTypeService,
    private val studentInfoRepository: StudentInfoRepository,
    private val eventMemoWriter: EventMemoWriter,
    private val contextHolder: ContextHolder
) {
    fun createEvent(request: CreateEventRequest): EventResponse {
        val users = findUsers(request.userIds)
        val checkpoints = findCheckpoints(request.checkpointIds)

        val event = eventRepository.save(
            EventEntity(name = request.name, date = request.date, createdBy = contextHolder.user)
        )
        saveRelations(event, users, checkpoints)
        eventMemoWriter.refresh(event.date, gradesOf(users))

        return EventResponse.of(event, checkpoints.map { it.name }, users.size)
    }

    fun updateEvent(eventId: Long, request: UpdateEventRequest): EventResponse {
        val event = getEventEntity(eventId)
        // 이번 수정으로 빠지는 학년의 메모도 갱신해야 하므로 변경 전 학년을 먼저 붙잡는다
        val previousGrades = gradesOf(participantsOf(eventId))

        val users = findUsers(request.userIds)
        val checkpoints = findCheckpoints(request.checkpointIds)

        clearRelations(event, eventId)
        event.name = request.name
        event.date = request.date
        saveRelations(event, users, checkpoints)

        eventMemoWriter.refresh(event.date, previousGrades + gradesOf(users))

        return EventResponse.of(event, checkpoints.map { it.name }, users.size)
    }

    fun deleteEvent(eventId: Long) {
        val event = getEventEntity(eventId)
        val grades = gradesOf(participantsOf(eventId))

        clearRelations(event, eventId)
        eventRepository.delete(event)

        eventMemoWriter.refresh(event.date, grades)
    }

    @Transactional(readOnly = true)
    fun getEvents(date: LocalDate?): List<EventResponse> {
        val targetDate = date ?: LocalDate.now(ZoneId.of("Asia/Seoul"))
        val events = eventRepository.findAllByDateOrderByIdAsc(targetDate)
        val eventIds = events.mapNotNull { it.id }
        if (eventIds.isEmpty()) return emptyList()

        val checkpointsByEventId = eventCheckpointRepository.findAllByEventIdIn(eventIds)
            .groupBy { it.event.id }
        val userCountByEventId = eventUserRepository.findAllByEventIdIn(eventIds)
            .groupingBy { it.event.id }
            .eachCount()

        return events.map { event ->
            EventResponse.of(
                entity = event,
                checkpointNames = checkpointsByEventId[event.id].orEmpty()
                    .sortedBy { it.checkpoint.startAt }
                    .map { it.checkpoint.name },
                studentCount = userCountByEventId[event.id] ?: 0
            )
        }
    }

    @Transactional(readOnly = true)
    fun getEvent(eventId: Long): EventDetailResponse {
        val event = getEventEntity(eventId)
        val checkpoints = eventCheckpointRepository.findAllByEventId(eventId)
            .sortedBy { it.checkpoint.startAt }
            .map { CheckpointSimpleResponse.of(it.checkpoint) }

        val users = participantsOf(eventId)
        val students = if (users.isEmpty()) {
            emptyList()
        } else {
            studentInfoRepository.findAllByUserIn(users)
                .sortedWith(compareBy({ it.grade }, { it.classNumber }, { it.num }))
                .map {
                    EventStudentResponse(
                        userId = it.user.id!!,
                        studentId = studentNumber(it),
                        name = it.user.name
                    )
                }
        }

        return EventDetailResponse.of(event, checkpoints, students)
    }

    private fun saveRelations(
        event: EventEntity,
        users: List<UserEntity>,
        checkpoints: List<AttendanceCheckpointEntity>
    ) {
        eventUserRepository.saveAll(users.map { EventUserEntity(event = event, user = it) })
        eventCheckpointRepository.saveAll(
            checkpoints.map { EventCheckpointEntity(event = event, checkpoint = it) }
        )
        createAttendances(event, users, checkpoints)
    }

    /**
     * 출석부·엑셀·필터는 attendance row가 없으면 "미출석"으로 본다.
     * 그래서 외박과 마찬가지로 행사 출석을 미리 만들어 둔다.
     */
    private fun createAttendances(
        event: EventEntity,
        users: List<UserEntity>,
        checkpoints: List<AttendanceCheckpointEntity>
    ) {
        val type = attendanceTypeService
            .getAttendanceTypeEntityByName(AttendanceTypeEntity.SCHOOL_EVENT_TYPE_NAME)

        val attendances = users.flatMap { user ->
            checkpoints.map { checkpoint ->
                attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, event.date)
                    ?.let { attendanceRepository.delete(it) }

                AttendanceEntity(
                    user = user,
                    checkpoint = checkpoint,
                    date = event.date,
                    type = type,
                    room = null,
                    event = event
                )
            }
        }
        attendanceRepository.saveAll(attendances)
    }

    private fun clearRelations(event: EventEntity, eventId: Long) {
        attendanceRepository.deleteAllByEvent(event)
        eventUserRepository.deleteAllByEventId(eventId)
        eventCheckpointRepository.deleteAllByEventId(eventId)
    }

    private fun findUsers(userIds: List<Long>): List<UserEntity> {
        val uniqueIds = userIds.distinct()
        if (uniqueIds.isEmpty()) throw CustomException(EventError.EMPTY_USERS)

        val users = userRepository.findAllByIdInAndIsDeletedFalse(uniqueIds)
        if (users.size != uniqueIds.size) throw CustomException(UserError.USER_NOT_FOUND)
        return users
    }

    private fun findCheckpoints(checkpointIds: List<Long>): List<AttendanceCheckpointEntity> {
        val uniqueIds = checkpointIds.distinct()
        if (uniqueIds.isEmpty()) throw CustomException(EventError.EMPTY_CHECKPOINTS)

        val checkpoints = checkpointRepository.findAllByIdInAndIsDeletedFalse(uniqueIds)
        if (checkpoints.size != uniqueIds.size) throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        return checkpoints
    }

    private fun participantsOf(eventId: Long): List<UserEntity> =
        eventUserRepository.findAllByEventId(eventId).map { it.user }

    private fun gradesOf(users: List<UserEntity>): Set<Int> {
        if (users.isEmpty()) return emptySet()
        return studentInfoRepository.findAllByUserIn(users).map { it.grade }.toSet()
    }

    private fun getEventEntity(eventId: Long): EventEntity =
        eventRepository.findByIdOrNull(eventId) ?: throw CustomException(EventError.EVENT_NOT_FOUND)

    private fun studentNumber(info: StudentInfoEntity): String =
        String.format("%d%d%02d", info.grade, info.classNumber, info.num)
}
