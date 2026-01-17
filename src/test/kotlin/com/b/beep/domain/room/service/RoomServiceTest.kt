package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.request.CreateRoomRequest
import com.b.beep.domain.room.controller.dto.request.UpdateRoomRequest
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.error.RoomError
import com.b.beep.domain.room.repository.RoomRepository
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
import java.util.*

@ExtendWith(MockitoExtension::class)
class RoomServiceTest {

    @Mock
    private lateinit var roomRepository: RoomRepository

    @InjectMocks
    private lateinit var roomService: RoomService

    private fun createRoomEntity(
        id: Long = 1L,
        name: String = "Room A",
        grade: Int? = 1,
        classNumber: Int? = 1,
        floor: Int? = 1
    ) = RoomEntity(
        id = id,
        name = name,
        grade = grade,
        classNumber = classNumber,
        floor = floor
    )

    @Nested
    @DisplayName("createRoom")
    inner class CreateRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val request = CreateRoomRequest(name = "Room A", grade = 1, classNumber = 1, floor = 1)
            val savedRoom = createRoomEntity()

            `when`(roomRepository.existsByName(request.name)).thenReturn(false)
            `when`(roomRepository.save(any<RoomEntity>())).thenReturn(savedRoom)

            val result = roomService.createRoom(request)

            assertEquals("Room A", result.name)
            verify(roomRepository).existsByName(request.name)
            verify(roomRepository).save(any<RoomEntity>())
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val request = CreateRoomRequest(name = "Room A", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.existsByName(request.name)).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                roomService.createRoom(request)
            }

            assertEquals(RoomError.ROOM_ALREADY_EXISTS, exception.error)
            verify(roomRepository, never()).save(any<RoomEntity>())
        }
    }

    @Nested
    @DisplayName("getRooms")
    inner class GetRooms {

        @Test
        @DisplayName("목록 조회")
        fun success() {
            val rooms = listOf(
                createRoomEntity(id = 1L, name = "Room A"),
                createRoomEntity(id = 2L, name = "Room B")
            )

            `when`(roomRepository.findAll()).thenReturn(rooms)

            val result = roomService.getRooms()

            assertEquals(2, result.size)
            assertEquals("Room A", result[0].name)
            assertEquals("Room B", result[1].name)
        }

        @Test
        @DisplayName("빈 목록")
        fun emptyList() {
            `when`(roomRepository.findAll()).thenReturn(emptyList<RoomEntity>())

            val result = roomService.getRooms()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("getRoom")
    inner class GetRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val room = createRoomEntity()

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))

            val result = roomService.getRoom(1L)

            assertEquals("Room A", result.name)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomService.getRoom(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("updateRoom")
    inner class UpdateRoom {

        @Test
        @DisplayName("성공 - 같은 이름")
        fun success_sameName() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room A", grade = 2, classNumber = 2, floor = 2)

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))

            val result = roomService.updateRoom(1L, request)

            assertEquals("Room A", result.name)
            assertEquals(2, result.grade)
            verify(roomRepository, never()).existsByName(any())
        }

        @Test
        @DisplayName("성공 - 새 이름")
        fun success_newName() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomRepository.existsByName("Room B")).thenReturn(false)

            val result = roomService.updateRoom(1L, request)

            assertEquals("Room B", result.name)
        }

        @Test
        @DisplayName("중복 이름 시 예외")
        fun duplicateName_throwsException() {
            val room = createRoomEntity()
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))
            `when`(roomRepository.existsByName("Room B")).thenReturn(true)

            val exception = assertThrows(CustomException::class.java) {
                roomService.updateRoom(1L, request)
            }

            assertEquals(RoomError.ROOM_ALREADY_EXISTS, exception.error)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            val request = UpdateRoomRequest(name = "Room B", grade = 1, classNumber = 1, floor = 1)

            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomService.updateRoom(1L, request)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
        }
    }

    @Nested
    @DisplayName("deleteRoom")
    inner class DeleteRoom {

        @Test
        @DisplayName("성공")
        fun success() {
            val room = createRoomEntity()

            `when`(roomRepository.findById(1L)).thenReturn(Optional.of(room))

            roomService.deleteRoom(1L)

            verify(roomRepository).delete(room)
        }

        @Test
        @DisplayName("없으면 예외")
        fun notFound_throwsException() {
            `when`(roomRepository.findById(1L)).thenReturn(Optional.empty())

            val exception = assertThrows(CustomException::class.java) {
                roomService.deleteRoom(1L)
            }

            assertEquals(RoomError.ROOM_NOT_FOUND, exception.error)
            verify(roomRepository, never()).delete(any<RoomEntity>())
        }
    }
}
