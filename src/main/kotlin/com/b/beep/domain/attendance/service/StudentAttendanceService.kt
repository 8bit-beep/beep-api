package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.AttendRequest
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.service.RoomService
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
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
) {
    fun attend(request: AttendRequest) {
        val user = contextHolder.user
        val checkpoint = checkpointResolver.getCurrentAttendableCheckpoint()
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val dayOfWeek = today.dayOfWeek
        val room = roomService.getRoomById(request.roomId)

        val schedule = getScheduleEntity(user, dayOfWeek, checkpoint, request.attendanceType)
            ?: throw CustomException(AttendanceError.SCHEDULE_NOT_FOUND)

        if (schedule.room.id != room.id) {
            throw CustomException(AttendanceError.ROOM_MISMATCH)
        }

        val attendance = attendanceRepository.findByUserIdAndCheckpointIdAndDate(user.id!!, checkpoint.id!!, today)
            ?: attendanceRepository.save(
                AttendanceEntity(
                    user = user,
                    checkpoint = checkpoint,
                    date = today,
                    type = AttendanceType.NOT_ATTEND
                )
            )

        if (attendance.type != AttendanceType.NOT_ATTEND) {
            throw CustomException(AttendanceError.ALREADY_ATTENDED)
        }

        attendance.type = request.attendanceType
        attendance.room = room
        attendanceRepository.save(attendance)
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
        attendanceRepository.findByUserIdAndCheckpointIdAndDate(user.id!!, checkpoint.id!!, LocalDate.now(ZoneId.of("Asia/Seoul")))

    private fun getScheduleEntity(
        user: UserEntity,
        dayOfWeek: DayOfWeek,
        checkpoint: AttendanceCheckpointEntity,
        type: AttendanceType
    ): StudentScheduleEntity? = studentScheduleRepository.findByUserAndDayOfWeekAndCheckpointAndType(
        user, dayOfWeek, checkpoint, type
    )
}
