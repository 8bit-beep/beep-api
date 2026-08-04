package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceSortModeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceSortModeResponse
import com.b.beep.domain.attendance.controller.dto.response.AttendanceSortModesResponse
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceSortModeEntity
import com.b.beep.domain.attendance.repository.AttendanceSortModeRepository
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class AttendanceSortModeService(
    private val attendanceSortModeRepository: AttendanceSortModeRepository,
    private val attendanceTypeService: AttendanceTypeService,
    private val checkpointResolver: CheckpointResolver
) {
    fun updateSortMode(request: UpdateAttendanceSortModeRequest): AttendanceSortModesResponse {
        val date = getToday()
        return updateSortMode(request, date)
    }

    internal fun updateSortMode(
        request: UpdateAttendanceSortModeRequest,
        date: LocalDate
    ): AttendanceSortModesResponse {
        val checkpoint = checkpointResolver.getCurrentCheckpointOrNearest(request.grade, date.dayOfWeek)

        if (request.typeId == null) {
            attendanceSortModeRepository.deleteByDateAndCheckpointAndGrade(date, checkpoint, request.grade)
        } else {
            val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)
            val sortMode = attendanceSortModeRepository.findByDateAndCheckpointAndGrade(
                date = date,
                checkpoint = checkpoint,
                grade = request.grade
            )

            if (sortMode == null) {
                attendanceSortModeRepository.save(
                    AttendanceSortModeEntity(
                        date = date,
                        checkpoint = checkpoint,
                        grade = request.grade,
                        type = type
                    )
                )
            } else {
                sortMode.type = type
            }
        }

        return getSortModes(date)
    }

    @Transactional(readOnly = true)
    fun getSortModes(): AttendanceSortModesResponse {
        return getSortModes(getToday())
    }

    internal fun getSortModes(date: LocalDate): AttendanceSortModesResponse {
        val checkpointByGrade = checkpointResolver.getCurrentCheckpointsOrNearest(GRADES, date.dayOfWeek)
        val modesByGradeAndCheckpoint = attendanceSortModeRepository.findAllByDateAndCheckpointIn(
            date = date,
            checkpoints = checkpointByGrade.values.distinctBy { it.id }
        ).associateBy { it.grade to it.checkpoint.id }

        return AttendanceSortModesResponse(
            date = date,
            modes = GRADES.map { grade ->
                val checkpoint = checkpointByGrade.getValue(grade)
                AttendanceSortModeResponse(
                    grade = grade,
                    checkpoint = CheckpointSimpleResponse.of(checkpoint),
                    type = modesByGradeAndCheckpoint[grade to checkpoint.id]?.type
                        ?.let { AttendanceTypeResponse.of(it) }
                )
            }
        )
    }

    private fun getToday(): LocalDate {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
    }

    companion object {
        private val GRADES = listOf(1, 2, 3)
    }
}
