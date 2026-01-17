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
    private lateinit var typeRepository: AttendanceTypeRepository

    @InjectMocks
    private lateinit var typeService: AttendanceTypeService

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

            `when`(typeRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(false)
            `when`(typeRepository.save(any<AttendanceTypeEntity>())).thenReturn(savedEntity)

            val result = typeService.create(request)

            assertEquals("PRESENT", result.name)
            verify(typeRepository).existsByNameAndIsDeletedFalse(request.name)
            verify(typeRepository).save(any<AttendanceTypeEntity>())
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val request = CreateAttendanceTypeRequest(name = "PRESENT")

            `when`(typeRepository.existsByNameAndIsDeletedFalse(request.name)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                typeService.create(request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS, exception.error)
            verify(typeRepository, never()).save(any<AttendanceTypeEntity>())
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

            `when`(typeRepository.findAllByIsDeletedFalse()).thenReturn(types)

            val result = typeService.findAll()

            assertEquals(2, result.size)
            assertEquals("PRESENT", result[0].name)
            assertEquals("ABSENT", result[1].name)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            `when`(typeRepository.findAllByIsDeletedFalse()).thenReturn(emptyList<AttendanceTypeEntity>())

            val result = typeService.findAll()

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

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            val result = typeService.findById(1L)

            assertEquals("PRESENT", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                typeService.findById(1L)
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

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            val result = typeService.update(1L, request)

            assertEquals("PRESENT", result.name)
            verify(typeRepository, never()).existsByNameAndIsDeletedFalse(any())
        }

        @Test
        @DisplayName("성공 - 새 이름")
        fun success_newName() {
            val entity = createTypeEntity()
            val request = UpdateAttendanceTypeRequest(name = "LATE")

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)
            `when`(typeRepository.existsByNameAndIsDeletedFalse("LATE")).thenReturn(false)

            val result = typeService.update(1L, request)

            assertEquals("LATE", result.name)
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val entity = createTypeEntity()
            val request = UpdateAttendanceTypeRequest(name = "ABSENT")

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)
            `when`(typeRepository.existsByNameAndIsDeletedFalse("ABSENT")).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                typeService.update(1L, request)
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_ALREADY_EXISTS, exception.error)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateAttendanceTypeRequest(name = "LATE")

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                typeService.update(1L, request)
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

            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(entity)

            typeService.delete(1L)

            assertTrue(entity.isDeleted)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(typeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                typeService.delete(1L)
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

            `when`(typeRepository.findByNameAndIsDeletedFalse("PRESENT")).thenReturn(entity)

            val result = typeService.getByName("PRESENT")

            assertEquals("PRESENT", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(typeRepository.findByNameAndIsDeletedFalse("UNKNOWN")).thenReturn(null)

            val exception = assertThrows(CustomException::class.java) {
                typeService.getByName("UNKNOWN")
            }

            assertEquals(AttendanceTypeError.ATTENDANCE_TYPE_NOT_FOUND, exception.error)
        }
    }
}
