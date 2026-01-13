package com.b.beep.domain.user.service

import com.b.beep.domain.user.controller.dto.request.CreateStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.error.StudentScheduleError
import com.b.beep.domain.user.entity.StudentScheduleEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StudentScheduleService(
    private val studentScheduleRepository: StudentScheduleRepository,
    private val studentScheduleValidator: StudentScheduleValidator,
    private val contextHolder: ContextHolder
) {
    fun create(request: CreateStudentScheduleRequest) {
        val user = contextHolder.user

        studentScheduleValidator.validateDayOfWeek(request.dayOfWeek)
        studentScheduleValidator.validatePeriod(request.period)
        studentScheduleValidator.validateNotDuplicate(user, request.dayOfWeek, request.period)

        val schedule = StudentScheduleEntity(
            user = user,
            dayOfWeek = request.dayOfWeek,
            period = request.period,
            type = request.type,
            room = request.room
        )
        studentScheduleRepository.save(schedule)
    }

    fun update(scheduleId: Long, request: UpdateStudentScheduleRequest) {
        val schedule = getScheduleOrThrow(scheduleId)
        val user = contextHolder.user

        validateOwnership(schedule, user)

        request.dayOfWeek?.let { studentScheduleValidator.validateDayOfWeek(it) }
        request.period?.let { studentScheduleValidator.validatePeriod(it) }

        val finalDayOfWeek = request.dayOfWeek ?: schedule.dayOfWeek
        val finalPeriod = request.period ?: schedule.period

        studentScheduleValidator.validateNotDuplicateExcluding(
            user, finalDayOfWeek, finalPeriod, scheduleId
        )

        request.dayOfWeek?.let { schedule.dayOfWeek = it }
        request.period?.let { schedule.period = it }
        request.type?.let { schedule.type = it }
        request.room?.let { schedule.room = it }

        studentScheduleRepository.save(schedule)
    }

    fun delete(scheduleId: Long) {
        val schedule = getScheduleOrThrow(scheduleId)
        val user = contextHolder.user

        validateOwnership(schedule, user)

        studentScheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<StudentScheduleEntity> {
        val user = contextHolder.user
        return studentScheduleRepository.findAllByUser(user)
    }

    private fun getScheduleOrThrow(scheduleId: Long): StudentScheduleEntity {
        return studentScheduleRepository.findByIdOrNull(scheduleId)
            ?: throw CustomException(StudentScheduleError.SCHEDULE_NOT_FOUND)
    }

    private fun validateOwnership(schedule: StudentScheduleEntity, user: com.b.beep.domain.user.entity.UserEntity) {
        if (schedule.user.id != user.id) {
            throw CustomException(StudentScheduleError.NO_PERMISSION)
        }
    }
}
