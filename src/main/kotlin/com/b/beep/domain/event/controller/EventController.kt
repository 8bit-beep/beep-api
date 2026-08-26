package com.b.beep.domain.event.controller

import com.b.beep.domain.event.controller.dto.request.CreateEventRequest
import com.b.beep.domain.event.controller.dto.request.UpdateEventRequest
import com.b.beep.domain.event.controller.dto.response.EventDetailResponse
import com.b.beep.domain.event.controller.dto.response.EventResponse
import com.b.beep.domain.event.service.EventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "교내 행사", description = "교내 행사 API")
@Validated
@RestController
@RequestMapping("/events")
class EventController(
    private val eventService: EventService
) {
    @Operation(
        summary = "행사 등록",
        description = """
            교내 행사를 등록합니다.

            - 선택한 학생 × 교시 조합의 출석을 "교내 행사" 타입으로 미리 생성합니다.
            - 참여 학생이 속한 학년의 메모에 행사 블록이 자동으로 기록됩니다.
            - 담당 교사는 요청 토큰에서 자동으로 채워집니다.
        """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEvent(@Valid @RequestBody request: CreateEventRequest): EventResponse {
        return eventService.createEvent(request)
    }

    @Operation(summary = "날짜별 행사 목록 조회", description = "date를 생략하면 오늘 날짜로 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getEvents(
        @Parameter(description = "조회 날짜 (yyyy-MM-dd)")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): List<EventResponse> {
        return eventService.getEvents(date)
    }

    @Operation(summary = "행사 상세 조회", description = "수정 화면에서 쓸 교시·학생 목록을 함께 반환합니다.")
    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    fun getEvent(
        @PathVariable @Positive(message = "행사 ID는 양수여야 합니다") eventId: Long
    ): EventDetailResponse {
        return eventService.getEvent(eventId)
    }

    @Operation(
        summary = "행사 수정",
        description = "기존 출석과 참여자를 모두 지우고 새 내용으로 다시 만듭니다. 빠진 학년의 메모도 함께 갱신됩니다."
    )
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateEvent(
        @PathVariable @Positive(message = "행사 ID는 양수여야 합니다") eventId: Long,
        @Valid @RequestBody request: UpdateEventRequest
    ): EventResponse {
        return eventService.updateEvent(eventId, request)
    }

    @Operation(summary = "행사 삭제", description = "행사가 만든 출석과 메모 블록도 함께 정리됩니다.")
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEvent(
        @PathVariable @Positive(message = "행사 ID는 양수여야 합니다") eventId: Long
    ) {
        eventService.deleteEvent(eventId)
    }
}
