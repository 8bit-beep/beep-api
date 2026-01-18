package com.b.beep.domain.user.controller.docs

import com.b.beep.domain.user.controller.dto.request.CreateMyScheduleRequest
import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

@Tag(name = "학생 스케줄", description = "학생 교시별 실 등록 API")
interface StudentScheduleDocs {
    @Operation(summary = "스케줄 생성", description = "특정 유저의 스케줄을 생성합니다.")
    fun createSchedule(@Valid request: CreateStudentScheduleRequest)

    @Operation(summary = "유저별 스케줄 조회", description = "특정 유저의 스케줄 목록을 조회합니다.")
    fun getSchedulesByUserId(@Parameter(description = "유저 ID") @Positive(message = "유저 ID는 양수여야 합니다") userId: Long): List<StudentScheduleResponse>

    @Operation(summary = "내 스케줄 생성", description = "로그인한 사용자의 스케줄을 생성합니다.")
    fun createMySchedule(@Valid request: CreateMyScheduleRequest)

    @Operation(summary = "내 스케줄 조회", description = "로그인한 사용자의 스케줄 목록을 조회합니다.")
    fun getMySchedules(): List<StudentScheduleResponse>

    @Operation(summary = "스케줄 수정", description = "스케줄 정보를 수정합니다.")
    fun updateSchedule(@Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long, @Valid request: UpdateStudentScheduleRequest)

    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다.")
    fun deleteSchedule(@Positive(message = "스케줄 ID는 양수여야 합니다") scheduleId: Long)
}
