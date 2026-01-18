package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.controller.dto.request.CreateAttendanceTypeRequest
import com.b.beep.domain.attendance.controller.dto.request.UpdateAttendanceTypeRequest
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceTypeError
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.global.exception.CustomException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class AttendanceTypeServiceTest {

    @Mock
    private lateinit var attendanceTypeRepository: AttendanceTypeRepository

    @InjectMocks
    private lateinit var attendanceTypeService: AttendanceTypeService

    private fun createTypeEntity(
        id: Long = 1L,
        name: String = "PRESENT",
        isDeleted: Boolean = false
    ) = AttendanceTypeEntity(id = id, name = name, isDeleted = isDeleted)

    @Nested
    @DisplayName("create")
    inner class Create {

        @Test
        @DisplayName("성공")
        fun success() {
            val request = CreateAttendanceTypeRequest(name = "PRESENT")
            val savedEntity = createTypeEntity()

            `when`(attendanceTypeRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(false)
            `when`(attendanceTypeRepository.save(any<AttendanceTypeEntity>())).thenReturn(savedEntity)

            val result = attendanceTypeService.createType(request)

            assertEquals("PRESENT", result.name)
            verify(attendanceTypeRepository).existsByNameAndIsDeletedFalse(request.name)
            verify(attendanceTypeRepository).save(any<AttendanceTypeEntity>())
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val request = CreateAttendanceTypeRequest(name = "PRESENT")

            `when`(attendanceTypeRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.createType(request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS, exception.error)
            verify(attendanceTypeRepository, never()).save(any<AttendanceTypeEntity>())
        }
    }

    @Nested
    @DisplayName("findAll")
    inner class FindAll {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val types = listOf(
                createTypeEntity(id = 1L, name = "PRESENT"),
                createTypeEntity(id = 2L, name = "ABSENT")
            )

            `when`(attendanceTypeRepository.findAllByIsDeletedFalse()).thenReturn(types)

            val result = attendanceTypeService.getAttendanceTypes()

            assertEquals(2, result.size)
            assertEquals("PRESENT", result[0].name)
            assertEquals("ABSENT", result[1].name)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            `when`(attendanceTypeRepository.findAllByIsDeletedFalse()).thenReturn(emptyList<AttendanceTypeEntity>())

            val result = attendanceTypeService.getAttendanceTypes()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        @DisplayName("성공")
        fun success() {
            val entity = createTypeEntity()

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            val result = attendanceTypeService.getAttendanceType(1L)

            assertEquals("PRESENT", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.getAttendanceType(1L)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("성공 - 같은 이름")
        fun success_sameName() {
            val entity = createTypeEntity()
            val request = UpdateAttendanceTypeRequest(name = "PRESENT")

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            val result = attendanceTypeService.updateType(1L, request)

            assertEquals("PRESENT", result.name)
            verify(attendanceTypeRepository, never()).existsByNameAndIsDeletedFalse(any())
        }

        @Test
        @DisplayName("성공 - 새 이름")
        fun success_newName() {
            val entity = createTypeEntity()
            val request = UpdateAttendanceTypeRequest(name = "LATE")

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)
            `when`(attendanceTypeRepository.existsByNameAndIsDeletedFalse("LATE")).thenReturn(false)

            val result = attendanceTypeService.updateType(1L, request)

            assertEquals("LATE", result.name)
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val entity = createTypeEntity()
            val request = UpdateAttendanceTypeRequest(name = "ABSENT")

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)
            `when`(attendanceTypeRepository.existsByNameAndIsDeletedFalse("ABSENT")).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.updateType(1L, request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS, exception.error)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateAttendanceTypeRequest(name = "LATE")

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.updateType(1L, request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("delete")
    inner class Delete {

        @Test
        @DisplayName("성공")
        fun success() {
            val entity = createTypeEntity()

            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            attendanceTypeService.deleteType(1L)

            assertTrue(entity.isDeleted)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(attendanceTypeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.deleteType(1L)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("getByName")
    inner class GetByName {

        @Test
        @DisplayName("성공")
        fun success() {
            val entity = createTypeEntity()

            `when`(attendanceTypeRepository.findByNameAndIsDeletedFalse("PRESENT")).thenReturn(entity)

            val result = attendanceTypeService.getAttendanceTypeEntityByName("PRESENT")

            assertEquals("PRESENT", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(attendanceTypeRepository.findByNameAndIsDeletedFalse("UNKNOWN")).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                attendanceTypeService.getAttendanceTypeEntityByName("UNKNOWN")
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
        }
    }
}
