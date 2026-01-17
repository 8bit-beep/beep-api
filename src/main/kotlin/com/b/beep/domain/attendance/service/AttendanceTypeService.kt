package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.response.AttendanceTypeResponse
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceTypeError
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AttendanceTypeService(
    private val attendanceTypeRepository: AttendanceTypeRepository
) {
    fun createType(request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        if (attendanceTypeRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS)
        }
        val entity = attendanceTypeRepository.save(AttendanceTypeEntity(name = request.name))
        return AttendanceTypeResponse.of(entity)
    }

    @Transactional(readOnly = true)
    fun getTypes(): List<AttendanceTypeResponse> {
        return attendanceTypeRepository.findAllByIsDeletedFalse().map { AttendanceTypeResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun getType(id: Long): AttendanceTypeResponse {
        val entity = getById(id)
        return AttendanceTypeResponse.of(entity)
    }

    fun updateType(id: Long, request: UpdateAttendanceTypeRequest): AttendanceTypeResponse {
        val entity = getById(id)
        if (entity.name != request.name && attendanceTypeRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS)
        }
        entity.name = request.name
        return AttendanceTypeResponse.of(entity)
    }

    fun deleteType(id: Long) {
        val entity = getById(id)
        entity.isDeleted = true
    }

    fun getById(id: Long): AttendanceTypeEntity {
        return attendanceTypeRepository.findByIdAndIsDeletedFalse(id)
            ?: throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND)
    }

    fun getByName(name: String): AttendanceTypeEntity {
        return attendanceTypeRepository.findByNameAndIsDeletedFalse(name)
            ?: throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND)
    }
}
