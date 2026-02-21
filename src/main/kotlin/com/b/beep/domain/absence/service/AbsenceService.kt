package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.request.AbsenceExceptionRequest
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.*
import com.b.beep.domain.absence.domain.AbsenceValidator
import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.domain.entity.AbsenceExceptionEntity
import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import com.b.beep.domain.absence.error.AbsenceError
import com.b.beep.domain.absence.repository.AbsenceExceptionRepository
import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.absence.repository.AbsenceUserRepository
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.error.CheckpointError
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.user.controller.dto.response.StudentInfoResponse
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.error.UserError
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.exception.CustomException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class AbsenceService(
    private val absenceRepository: AbsenceRepository,
    private val absenceUserRepository: AbsenceUserRepository,
    private val absenceExceptionRepository: AbsenceExceptionRepository,
    private val userRepository: UserRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val attendanceRepository: AttendanceRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val absenceValidator: AbsenceValidator,
    private val attendanceTypeService: AttendanceTypeService,
) {
    fun createAbsence(request: CreateAbsenceRequest): CreateAbsenceResponse {
        absenceValidator.validateDateRange(request.startDate, request.endDate)
        request.checkpoints?.forEach {
            absenceValidator.validateDateInRange(it.date, request.startDate, request.endDate)
        }

        val users = getUsers(request.userIds)
        val (validUsers, skippedUserIds) = partitionByOverlap(users, request.startDate, request.endDate)

        if (validUsers.isEmpty()) {
            return CreateAbsenceResponse(absenceId = null, skippedUserIds = skippedUserIds)
        }

        val absence = saveAbsence(request, validUsers)
        return CreateAbsenceResponse(absenceId = absence.id, skippedUserIds = skippedUserIds)
    }

    private fun getUsers(userIds: List<Long>): List<UserEntity> {
        val uniqueUserIds = userIds.distinct()
        val users = userRepository.findAllByIdInAndIsDeletedFalse(uniqueUserIds)
        if (users.size != uniqueUserIds.size) {
            throw CustomException(UserError.USER_NOT_FOUND)
        }
        return users
    }

    private fun partitionByOverlap(
        users: List<UserEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        excludeAbsenceId: Long? = null
    ): Pair<List<UserEntity>, List<Long>> {
        val (valid, skipped) = users.partition { user ->
            if (excludeAbsenceId != null) {
                !absenceValidator.existsOverlappingAbsenceExcluding(user.id!!, startDate, endDate, excludeAbsenceId)
            } else {
                !absenceValidator.existsOverlappingAbsence(user.id!!, startDate, endDate)
            }
        }
        return valid to skipped.map { it.id!! }
    }

    private fun saveAbsence(
        request: CreateAbsenceRequest,
        users: List<UserEntity>
    ): AbsenceEntity {
        val type = request.typeId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) }
        val absence = absenceRepository.save(
            AbsenceEntity(
                startDate = request.startDate,
                endDate = request.endDate,
                reason = request.reason,
                type = type
            )
        )
        saveAbsenceRelations(absence, users, request.startDate, request.endDate, request.checkpoints)
        return absence
    }

    private fun saveAbsenceRelations(
        absence: AbsenceEntity,
        users: List<UserEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        exceptions: List<AbsenceExceptionRequest>?
    ) {
        users.forEach { absenceUserRepository.save(AbsenceUserEntity(user = it, absence = absence)) }

        val exceptionEntities = saveAbsenceExceptions(absence, exceptions)

        createAttendancesForAbsence(absence, users, startDate, endDate, exceptionEntities)
    }

    private fun saveAbsenceExceptions(
        absence: AbsenceEntity,
        exceptions: List<AbsenceExceptionRequest>?
    ): List<AbsenceExceptionEntity> {
        if (exceptions.isNullOrEmpty()) {
            return emptyList()
        }
        val checkpointIds = exceptions.map { it.checkpointId }
        val checkpoints = checkpointRepository.findAllByIdInAndIsDeletedFalse(checkpointIds)
        if (checkpoints.size != checkpointIds.size) {
            throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
        }
        val checkpointMap = checkpoints.associateBy { it.id }

        val exceptionEntities = exceptions.map {
            AbsenceExceptionEntity(
                absence = absence,
                checkpoint = checkpointMap[it.checkpointId]!!,
                date = it.date
            )
        }
        return absenceExceptionRepository.saveAll(exceptionEntities)
    }


    @Transactional(readOnly = true)
    fun getAbsences(pageable: Pageable): Page<AbsenceResponse> {
        val page = absenceRepository.findAllByIsDeletedFalseOrderByStartDateAscEndDateAsc(pageable)

        val sorted = page.content.map { it.toResponse() }
            .map { it.copy(targetStudents = it.targetStudents.sortedWith(compareBy({ it.info?.grade ?: Int.MAX_VALUE }, { it.info?.classNumber ?: Int.MAX_VALUE }, { it.info?.num ?: Int.MAX_VALUE }))) }
            .sortedWith(compareBy({ it.startDate }, { it.endDate }, { it.targetStudents.firstOrNull()?.info?.grade ?: Int.MAX_VALUE }, { it.targetStudents.firstOrNull()?.info?.classNumber ?: Int.MAX_VALUE }, { it.targetStudents.firstOrNull()?.info?.num ?: Int.MAX_VALUE }))

        return PageImpl(sorted, pageable, page.totalElements)
    }

    fun updateAbsence(absenceId: Long, request: UpdateAbsenceRequest): UpdateAbsenceResponse {
        val absence = absenceRepository.findByIdAndIsDeletedFalse(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        absenceValidator.validateDateRange(request.startDate, request.endDate)
        request.checkpoints?.forEach {
            absenceValidator.validateDateInRange(it.date, request.startDate, request.endDate)
        }

        val users = getUsers(request.userIds)
        val (validUsers, skippedUserIds) = partitionByOverlap(users, request.startDate, request.endDate, absenceId)

        if (validUsers.isEmpty()) {
            return UpdateAbsenceResponse(absenceId = absenceId, skippedUserIds = skippedUserIds)
        }

        clearAbsenceRelations(absence, absenceId)
        updateAbsenceEntity(absence, request)
        saveAbsenceRelations(absence, validUsers, request.startDate, request.endDate, request.checkpoints)

        return UpdateAbsenceResponse(absenceId = absenceId, skippedUserIds = skippedUserIds)
    }

    private fun clearAbsenceRelations(absence: AbsenceEntity, absenceId: Long) {
        attendanceRepository.deleteAllByAbsence(absence)
        absenceUserRepository.deleteAllByAbsenceId(absenceId)
        absenceExceptionRepository.deleteAllByAbsenceId(absenceId)
    }

    private fun updateAbsenceEntity(
        absence: AbsenceEntity,
        request: UpdateAbsenceRequest
    ) {
        absence.startDate = request.startDate
        absence.endDate = request.endDate
        absence.reason = request.reason
        absence.type = request.typeId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) }
        absenceRepository.save(absence)
    }

    fun deleteAbsence(absenceId: Long) {
        val absence = absenceRepository.findByIdAndIsDeletedFalse(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        attendanceRepository.deleteAllByAbsence(absence)

        absenceUserRepository.deleteAllByAbsenceId(absenceId)
        absenceExceptionRepository.deleteAllByAbsenceId(absenceId)
        absence.isDeleted = true
    }

    private fun createAttendancesForAbsence(
        absence: AbsenceEntity,
        users: List<UserEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        exceptions: List<AbsenceExceptionEntity>
    ) {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val defaultAbsenceType =
            attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.DEFAULT_ABSENCE_TYPE_NAME)

        val datesToProcess = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .filter { !it.isBefore(today) }
            .toList()

        if (datesToProcess.isEmpty()) return

        val dayOfWeeks = datesToProcess.map { it.dayOfWeek }.distinct()

        val allSchedules = studentScheduleRepository.findAllByUserInAndDayOfWeekIn(users, dayOfWeeks)
            .groupBy { Triple(it.user.id, it.checkpoint.id, it.dayOfWeek) }

        val allCheckpoints = checkpointRepository.findAllByIsDeletedFalse()
        val exceptionsSet = exceptions.map { Triple(it.absence.id, it.checkpoint.id, it.date) }.toSet()

        val attendancesToSave = mutableListOf<AttendanceEntity>()

        for (date in datesToProcess) {
            for (user in users) {
                for (checkpoint in allCheckpoints) {
                    if (exceptionsSet.contains(Triple(absence.id, checkpoint.id, date))) {
                        continue
                    }

                    val schedule = allSchedules[Triple(user.id, checkpoint.id, date.dayOfWeek)]?.firstOrNull()
                    val attendanceType = absence.type ?: schedule?.type ?: defaultAbsenceType

                    val existing = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, date)
                    if (existing != null) {
                        attendanceRepository.delete(existing)
                    }

                    attendancesToSave.add(
                        AttendanceEntity(
                            user = user,
                            checkpoint = checkpoint,
                            date = date,
                            type = attendanceType,
                            room = schedule?.room,
                            absence = absence
                        )
                    )
                }
            }
        }

        attendanceRepository.saveAll(attendancesToSave)
    }

    private fun AbsenceEntity.toResponse(): AbsenceResponse {
        val id = this.id ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)
        val absenceUsers = absenceUserRepository.findAllByAbsenceId(id)
        val absenceExceptions = absenceExceptionRepository.findAllByAbsenceId(id)

        val studentResponses = absenceUsers.map { absenceUser ->
            val studentInfo = studentInfoRepository.findByUser(absenceUser.user)
            AbsenceStudentResponse(
                name = absenceUser.user.username,
                info = studentInfo?.let { StudentInfoResponse.of(it) }
            )
        }

        val exceptionResponses = absenceExceptions.map {
            AbsenceExceptionResponse.of(it)
        }

        return AbsenceResponse(
            absenceId = id,
            isGrouped = absenceUsers.size > 1,
            targetStudents = studentResponses,
            startDate = this.startDate,
            endDate = this.endDate,
            checkpoints = exceptionResponses,
            reason = this.reason,
            typeId = this.type?.id
        )
    }
}
