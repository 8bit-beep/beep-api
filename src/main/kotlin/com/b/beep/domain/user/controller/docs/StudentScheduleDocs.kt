package com.b.beep.domain.user.controller.docs

import com.b.beep.domain.user.controller.dto.request.AddStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.response.StudentScheduleResponse
import com.b.beep.global.common.dto.response.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "학생 스케줄", description = "학생 교시별 실 등록 API")
interface StudentScheduleDocs {
    @Operation(summary = "스케줄 추가", description = "교시별 실을 등록합니다.")
    fun addSchedule(request: AddStudentScheduleRequest)

    @Operation(summary = "내 스케줄 조회", description = "로그인한 사용자의 스케줄 목록을 조회합니다.")
    fun getMySchedules(): ResponseEntity<BaseResponse<List<StudentScheduleResponse>>>

    @Operation(summary = "스케줄 수정", description = "스케줄 정보를 수정합니다.")
    fun updateSchedule(scheduleId: Long, request: UpdateStudentScheduleRequest)

    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다.")
    fun deleteSchedule(scheduleId: Long)
}
