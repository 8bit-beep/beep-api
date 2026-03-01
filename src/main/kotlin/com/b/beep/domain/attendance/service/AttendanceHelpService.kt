package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.ScanHelpQrRequest
import com.b.beep.domain.attendance.controller.dto.response.HelpQrResponse
import com.b.beep.domain.attendance.domain.CheckpointResolver
import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.HelpQrTokenRepository
import com.b.beep.domain.room.service.RoomService
import com.b.beep.global.exception.CustomException
import com.b.beep.global.security.ContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional
class AttendanceHelpService(
    private val helpQrTokenRepository: HelpQrTokenRepository,
    private val attendanceRepository: AttendanceRepository,
    private val checkpointResolver: CheckpointResolver,
    private val contextHolder: ContextHolder,
    private val roomService: RoomService,
    private val attendanceTypeService: AttendanceTypeService,
) {
    fun generateHelpQr(): HelpQrResponse {
        val helper = contextHolder.user
        val checkpoint = checkpointResolver.getCurrentAttendableCheckpoint()
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))

        val attendance = attendanceRepository.findByUserIdAndCheckpointIdAndDate(
            helper.id!!, checkpoint.id!!, today
        ) ?: throw CustomException(AttendanceError.HELPER_NOT_ATTENDED)

        if (attendance.type.name == AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME) {
            throw CustomException(AttendanceError.HELPER_NOT_ATTENDED)
        }

        val token = helpQrTokenRepository.save(
            helperId = helper.id!!,
            checkpointId = checkpoint.id!!,
            roomId = attendance.room!!.id!!
        )

        return HelpQrResponse(token = token)
    }

    fun scanHelpQr(request: ScanHelpQrRequest) {
        val user = contextHolder.user
        val tokenData = helpQrTokenRepository.findByToken(request.token)
            ?: throw CustomException(AttendanceError.INVALID_HELP_QR)

        val checkpoint = checkpointResolver.getCurrentAttendableCheckpoint()
        if (checkpoint.id != tokenData.checkpointId) {
            throw CustomException(AttendanceError.INVALID_HELP_QR)
        }

        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val room = roomService.getRoomEntityById(tokenData.roomId)
        val type = attendanceTypeService.getAttendanceTypeEntityById(request.typeId)
        val notAttendType = attendanceTypeService.getAttendanceTypeEntityByName(
            AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME
        )

        val attendance = attendanceRepository.findByUserIdAndCheckpointIdAndDate(
            user.id!!, checkpoint.id!!, today
        ) ?: attendanceRepository.save(
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

        attendance.type = type
        attendance.room = room
        attendanceRepository.save(attendance)

        helpQrTokenRepository.delete(request.token)
    }
}
