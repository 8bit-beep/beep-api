package com.b.beep.domain.shift.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.enums.AttendanceType
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.shift.domain.entity.ShiftEntity
import com.b.beep.domain.shift.domain.enums.ShiftStatus
import com.b.beep.domain.shift.error.ShiftError
import com.b.beep.domain.shift.repository.ShiftRepository
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class ShiftManagementService(
    private val shiftRepository: ShiftRepository,
    private val attendanceRepository: AttendanceRepository,
) {
    @Transactional(readOnly = true)
    fun getAll(): List<ShiftEntity> {
        return shiftRepository.findAllByDateGreaterThanEqual(LocalDate.now())
    }

    fun updateStatus(id: Long, status: ShiftStatus) {
        val shift = shiftRepository.findByIdOrNull(id)
            ?: throw CustomException(ShiftError.SHIFT_NOT_FOUND)

        shift.status = status

        createAttendance(shift.checkpoint, shift.user, shift.room, shift.date, status)

        shiftRepository.save(shift)
    }

    private fun createAttendance(
        checkpoint: AttendanceCheckpointEntity,
        user: UserEntity,
        room: RoomEntity,
        date: LocalDate,
        status: ShiftStatus
    ) {
        val attendance = attendanceRepository.findByCheckpointAndUserAndDate(checkpoint, user, date)

        when (status) {
            ShiftStatus.APPROVED -> {
                val record = attendance ?: AttendanceEntity(
                    user = user,
                    checkpoint = checkpoint,
                    date = date,
                    type = AttendanceType.SHIFT_ATTEND,
                    room = room
                )
                record.type = AttendanceType.SHIFT_ATTEND
                record.room = room
                attendanceRepository.save(record)
            }

            ShiftStatus.WAITING, ShiftStatus.REJECTED -> {
                attendance?.let { attendanceRepository.delete(it) }
            }
        }
    }
}
