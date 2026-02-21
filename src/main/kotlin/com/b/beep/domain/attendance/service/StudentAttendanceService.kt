package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class StudentAttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val contextHolder: ContextHolder,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val checkpointResolver: CheckpointResolver,
    private val roomService: RoomService,
    private val attendanceTypeService: AttendanceTypeService,
    private val roomApprovalRepository: RoomApprovalRepository,
) {
    fun attend(request: CreateAttendanceRequest) {
        val user = contextHolder.user
        val checkpoint = checkpointResolver.getCurrentAttendableCheckpoint()
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val dayOfWeek = today.dayOfWeek
        val room = roomService.getRoomEntityById(request.roomId)
        val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)
        val notAttendType =
            attendanceTypeService.getAttendanceTypeEntityByName(AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME)

        getOrCreateSchedule(user, dayOfWeek, checkpoint, type, room)

        val attendance = attendanceRepository.findByUserIdAndCheckpointIdAndDate(user.id!!, checkpoint.id!!, today)
            ?: attendanceRepository.saveAndFlush(
                AttendanceEntity(
                    user = user,
                    checkpoint = checkpoint,
                    date = today,
                    type = notAttendType
                )
            )

        if (attendance.type.name != AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME) {
            throw CustomException(AttendanceError.ALREADY_ATTENDED)
        }

        val approval = roomApprovalRepository.findByCheckpointAndRoomAndDate(checkpoint, room, today)
        val isLate = approval != null && (attendance.createdAt?.isAfter(approval.createdAt) == true)

        attendance.type = type
        attendance.room = room
        attendance.isLate = isLate

        try {
            attendanceRepository.save(attendance)
        } catch (e: OptimisticLockingFailureException) {
            throw CustomException(AttendanceError.CONCURRENT_MODIFICATION)
        }
    }

    fun cancelAttendance() {
        val user = contextHolder.user
        val checkpoint = checkpointResolver.getCurrentAttendableCheckpoint()

        val attendance = getAttendanceEntity(user, checkpoint)
            ?: throw CustomException(AttendanceError.ATTENDANCE_NOT_FOUND)
        attendanceRepository.delete(attendance)
    }

    private fun getAttendanceEntity(
        user: UserEntity,
        checkpoint: AttendanceCheckpointEntity
    ): AttendanceEntity? =
        attendanceRepository.findByUserIdAndCheckpointIdAndDate(
            user.id!!,
            checkpoint.id!!,
            LocalDate.now(ZoneId.of("Asia/Seoul"))
        )

    private fun getOrCreateSchedule(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity,
        type: AttendanceTypeEntity,
        room: RoomEntity
    ): StudentScheduleEntity {
        val existingSchedule =
            studentScheduleRepository.findByUserAndDayOfWeekAndCheckpoint(user, dayOfWeek, checkpoint)

        if (existingSchedule != null) {
            if (existingSchedule.type.id != type.id) {
                throw CustomException(AttendanceError.TYPE_MISMATCH)
            }
            if (existingSchedule.room.id != room.id) {
                throw CustomException(AttendanceError.ROOM_MISMATCH)
            }
            return existingSchedule
        }

        return studentScheduleRepository.save(
            StudentScheduleEntity(
                user = user,
                dayOfWeek = dayOfWeek,
                checkpoint = checkpoint,
                type = type,
                room = room
            )
        )
    }
}
