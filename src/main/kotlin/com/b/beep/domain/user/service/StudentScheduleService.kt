package com.b.beep.domain.user.service

import com.b.beep.domain.user.controller.dto.request.AddStudentScheduleRequest
import com.b.beep.domain.user.controller.dto.request.UpdateStudentScheduleRequest
import com.b.beep.domain.user.domain.StudentScheduleError
import com.b.beep.domain.user.entity.StudentScheduleEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek

@Service
@Transactional
class StudentScheduleService(
    private val studentScheduleRepository: StudentScheduleRepository,
    private val contextHolder: ContextHolder
) {
    fun add(request: AddStudentScheduleRequest) {
        val user = contextHolder.user

        validateDayOfWeek(request.dayOfWeek)
        validatePeriod(request.period)

        if (studentScheduleRepository.existsByUserAndDayOfWeekAndPeriod(
                user, request.dayOfWeek, request.period
            )) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }

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
        val schedule = studentScheduleRepository.findByIdOrNull(scheduleId)
            ?: throw CustomException(StudentScheduleError.SCHEDULE_NOT_FOUND)
        val user = contextHolder.user

        if (schedule.user.id != user.id) {
            throw CustomException(StudentScheduleError.NO_PERMISSION)
        }

        request.dayOfWeek?.let { validateDayOfWeek(it) }
        request.period?.let { validatePeriod(it) }

        val finalDayOfWeek = request.dayOfWeek ?: schedule.dayOfWeek
        val finalPeriod = request.period ?: schedule.period

        val existingSchedules = studentScheduleRepository.findAllByUser(user)
        val conflict = existingSchedules.any {
            it.id != scheduleId &&
            it.dayOfWeek == finalDayOfWeek &&
            it.period == finalPeriod
        }
        if (conflict) {
            throw CustomException(StudentScheduleError.ALREADY_EXIST_SCHEDULE)
        }

        request.dayOfWeek?.let { schedule.dayOfWeek = it }
        request.period?.let { schedule.period = it }
        request.type?.let { schedule.type = it }
        request.room?.let { schedule.room = it }

        studentScheduleRepository.save(schedule)
    }

    fun delete(scheduleId: Long) {
        val schedule = studentScheduleRepository.findByIdOrNull(scheduleId)
            ?: throw CustomException(StudentScheduleError.SCHEDULE_NOT_FOUND)
        val user = contextHolder.user

        if (schedule.user.id != user.id) {
            throw CustomException(StudentScheduleError.NO_PERMISSION)
        }

        studentScheduleRepository.delete(schedule)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<StudentScheduleEntity> {
        val user = contextHolder.user
        return studentScheduleRepository.findAllByUser(user)
    }

    private fun validateDayOfWeek(dayOfWeek: DayOfWeek) {
        if (dayOfWeek !in listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)) {
            throw CustomException(StudentScheduleError.INVALID_DAY_OF_WEEK)
        }
    }

    private fun validatePeriod(period: Int) {
        if (period !in listOf(1, 2, 3)) {
            throw CustomException(StudentScheduleError.INVALID_PERIOD)
        }
    }
}
