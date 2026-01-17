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
    private val typeRepository: AttendanceTypeRepository
) {
    fun create(request: CreateAttendanceTypeRequest): AttendanceTypeResponse {
        if (typeRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS)
        }
        val entity = typeRepository.save(AttendanceTypeEntity(name = request.name))
        return AttendanceTypeResponse.of(entity)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<AttendanceTypeResponse> {
        return typeRepository.findAllByIsDeletedFalse().map { AttendanceTypeResponse.of(it) }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): AttendanceTypeResponse {
        val entity = getById(id)
        return AttendanceTypeResponse.of(entity)
    }

    fun update(id: Long, request: UpdateAttendanceTypeRequest): AttendanceTypeResponse {
        val entity = getById(id)
        if (entity.name != request.name && typeRepository.existsByNameAndIsDeletedFalse(request.name)) {
            throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS)
        }
        entity.name = request.name
        return AttendanceTypeResponse.of(entity)
    }

    fun delete(id: Long) {
        val entity = getById(id)
        entity.isDeleted = true
    }

    fun getById(id: Long): AttendanceTypeEntity {
        return typeRepository.findByIdAndIsDeletedFalse(id)
            ?: throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND)
    }

    fun getByName(name: String): AttendanceTypeEntity {
        return typeRepository.findByNameAndIsDeletedFalse(name)
            ?: throw CustomException(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND)
    }
}
