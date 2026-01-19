package com.b.beep.domain.absence.service

import com.b.beep.domain.absence.controller.dto.request.CheckpointSetting
import com.b.beep.domain.absence.controller.dto.request.CreateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.request.UpdateAbsenceRequest
import com.b.beep.domain.absence.controller.dto.response.AbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.AbsenceStudentResponse
import com.b.beep.domain.absence.controller.dto.response.CreateAbsenceResponse
import com.b.beep.domain.absence.controller.dto.response.UpdateAbsenceResponse
import com.b.beep.domain.absence.domain.AbsenceValidator
import com.b.beep.domain.absence.domain.entity.AbsenceCheckpointEntity
import com.b.beep.domain.absence.domain.entity.AbsenceEntity
import com.b.beep.domain.absence.domain.entity.AbsenceUserEntity
import com.b.beep.domain.absence.error.AbsenceError
import com.b.beep.domain.absence.repository.AbsenceCheckpointRepository
import com.b.beep.domain.absence.repository.AbsenceRepository
import com.b.beep.domain.absence.repository.AbsenceUserRepository
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.service.AttendanceTypeService
import com.b.beep.domain.checkpoint.controller.dto.response.CheckpointSimpleResponse
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class AbsenceService(
    private val absenceRepository: AbsenceRepository,
    private val absenceUserRepository: AbsenceUserRepository,
    private val absenceCheckpointRepository: AbsenceCheckpointRepository,
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

        val users = getUsers(request.userIds)
        val (validUsers, skippedUserIds) = partitionByOverlap(users, request.startDate, request.endDate)

        if (validUsers.isEmpty()) {
            return CreateAbsenceResponse(absenceId = null, skippedUserIds = skippedUserIds)
        }

        val type = request.typeId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) }
        val absence = saveAbsence(request, validUsers, type)
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
        users: List<UserEntity>,
        type: AttendanceTypeEntity?
    ): AbsenceEntity {
        val absence = absenceRepository.save(
            AbsenceEntity(
                startDate = request.startDate,
                endDate = request.endDate,
                reason = request.reason,
                type = type
            )
        )
        saveAbsenceRelations(absence, users, request.startDate, request.endDate, request.checkpoints, type)
        return absence
    }

    private fun saveAbsenceRelations(
        absence: AbsenceEntity,
        users: List<UserEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        checkpointSettings: List<CheckpointSetting>?,
        defaultType: AttendanceTypeEntity?
    ) {
        users.forEach { absenceUserRepository.save(AbsenceUserEntity(user = it, absence = absence)) }

        val checkpointsWithTypes = resolveCheckpointsWithTypes(checkpointSettings, defaultType)
        checkpointsWithTypes.forEach { (checkpoint, type) ->
            absenceCheckpointRepository.save(
                AbsenceCheckpointEntity(
                    checkpoint = checkpoint,
                    absence = absence,
                    type = type
                )
            )
        }

        createAttendancesForAbsence(absence, users, startDate, endDate, checkpointsWithTypes)
    }

    @Transactional(readOnly = true)
    fun getAbsences(pageable: Pageable): Page<AbsenceResponse> {
        return absenceRepository.findAllByIsDeletedFalse(pageable).map { it.toResponse() }
    }

    fun updateAbsence(absenceId: Long, request: UpdateAbsenceRequest): UpdateAbsenceResponse {
        val absence = absenceRepository.findByIdAndIsDeletedFalse(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        absenceValidator.validateDateRange(request.startDate, request.endDate)

        val users = getUsers(request.userIds)
        val (validUsers, skippedUserIds) = partitionByOverlap(users, request.startDate, request.endDate, absenceId)

        if (validUsers.isEmpty()) {
            return UpdateAbsenceResponse(absenceId = absenceId, skippedUserIds = skippedUserIds)
        }

        val type = request.typeId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) }

        clearAbsenceRelations(absence, absenceId)
        updateAbsenceEntity(absence, request, type)
        saveAbsenceRelations(absence, validUsers, request.startDate, request.endDate, request.checkpoints, type)

        return UpdateAbsenceResponse(absenceId = absenceId, skippedUserIds = skippedUserIds)
    }

    private fun clearAbsenceRelations(absence: AbsenceEntity, absenceId: Long) {
        attendanceRepository.deleteAllByAbsence(absence)
        absenceUserRepository.deleteAllByAbsenceId(absenceId)
        absenceCheckpointRepository.deleteAllByAbsenceId(absenceId)
    }

    private fun updateAbsenceEntity(
        absence: AbsenceEntity,
        request: UpdateAbsenceRequest,
        type: AttendanceTypeEntity?
    ) {
        absence.startDate = request.startDate
        absence.endDate = request.endDate
        absence.reason = request.reason
        absence.type = type
        absenceRepository.save(absence)
    }

    fun deleteAbsence(absenceId: Long) {
        val absence = absenceRepository.findByIdAndIsDeletedFalse(absenceId)
            ?: throw CustomException(AbsenceError.ABSENCE_NOT_FOUND)

        attendanceRepository.deleteAllByAbsence(absence)

        absenceUserRepository.deleteAllByAbsenceId(absenceId)
        absenceCheckpointRepository.deleteAllByAbsenceId(absenceId)
        absence.isDeleted = true
    }

    private fun resolveCheckpointsWithTypes(
        checkpointSettings: List<CheckpointSetting>?,
        defaultType: AttendanceTypeEntity?
    ): List<Pair<AttendanceCheckpointEntity, AttendanceTypeEntity?>> {
        return if (!checkpointSettings.isNullOrEmpty()) {
            val checkpointIds = checkpointSettings.map { it.checkpointId }
            val checkpoints = checkpointRepository.findAllById(checkpointIds).filter { !it.isDeleted }
            if (checkpoints.size != checkpointIds.size) {
                throw CustomException(CheckpointError.CHECKPOINT_NOT_FOUND)
            }
            val checkpointMap = checkpoints.associateBy { it.id }
            checkpointSettings.map { setting ->
                val checkpoint = checkpointMap[setting.checkpointId]!!
                val type = setting.typeId?.let { attendanceTypeService.getAttendanceTypeEntityById(it) } ?: defaultType
                checkpoint to type
            }
        } else {
            checkpointRepository.findAllByIsDeletedFalse().map { it to defaultType }
        }
    }

    private fun createAttendancesForAbsence(
        absence: AbsenceEntity,
        users: List<UserEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        checkpointsWithTypes: List<Pair<AttendanceCheckpointEntity, AttendanceTypeEntity?>>
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

        val attendancesToSave = mutableListOf<AttendanceEntity>()

        for (date in datesToProcess) {
            for (user in users) {
                for ((checkpoint, overrideType) in checkpointsWithTypes) {
                    val schedule = allSchedules[Triple(user.id, checkpoint.id, date.dayOfWeek)]?.firstOrNull()
                    val attendanceType = overrideType ?: schedule?.type ?: defaultAbsenceType

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
        val absenceCheckpoints = absenceCheckpointRepository.findAllByAbsenceId(id)

        val studentResponses = absenceUsers.map { absenceUser ->
            val studentInfo = studentInfoRepository.findByUser(absenceUser.user)
            AbsenceStudentResponse(
                name = absenceUser.user.username,
                info = studentInfo?.let { StudentInfoResponse.of(it) }
            )
        }

        val checkpointResponses = absenceCheckpoints.map {
            CheckpointSimpleResponse.of(it.checkpoint)
        }

        return AbsenceResponse(
            absenceId = id,
            isGrouped = absenceUsers.size > 1,
            targetStudents = studentResponses,
            startDate = this.startDate,
            endDate = this.endDate,
            checkpoints = checkpointResponses,
            reason = this.reason,
            type = this.type?.let { AttendanceTypeResponse.of(it) }
        )
    }
}
