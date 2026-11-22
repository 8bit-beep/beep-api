package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.UserEntity
import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.global.exception.CustomException
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class AttendanceHistoryServiceTest {
    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var s3Presigner: S3Presigner

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var attendanceTypeRepository: AttendanceTypeRepository

    @Mock
    private lateinit var studentInfoRepository: StudentInfoRepository

    @Mock
    private lateinit var checkpointRepository: AttendanceCheckpointRepository

    @Mock
    private lateinit var roomRepository: RoomRepository

    @Mock
    private lateinit var roomApprovalRepository: RoomApprovalRepository

    @Mock
    private lateinit var attendancePlacementService: AttendancePlacementService

    private lateinit var service: AttendanceHistoryService

    private val date = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)
    private val checkpoint = AttendanceCheckpointEntity(
        id = 1L, name = "저녁", startAt = LocalTime.of(20, 0), endAt = LocalTime.of(21, 0),
        attendanceStartAt = LocalTime.of(19, 50), attendanceEndAt = LocalTime.of(20, 10)
    )
    private val type = AttendanceTypeEntity(id = 1L, name = "출석")

    @BeforeEach
    fun setUp() {
        service = AttendanceHistoryService(
            s3Client, s3Presigner, attendanceRepository, attendanceTypeRepository,
            studentInfoRepository, checkpointRepository, roomRepository,
            roomApprovalRepository, attendancePlacementService, "test-bucket"
        )
    }

    @Test
    fun `과거 Excel은 자퇴생의 이름 학번 출석을 학년과 출석실 시트에 보존한다`() {
        val active = student(1L, "재학생", 1)
        val withdrawn = student(2L, "자퇴생", 2, isDeleted = true)
        val room = RoomEntity(id = 1L, name = "자습실", floor = 1)
        val attendances = listOf(active, withdrawn).map {
            AttendanceEntity(checkpoint = checkpoint, type = type, user = it.user, date = date, room = room)
        }
        stubExport(listOf(active), attendances, listOf(room))
        whenever(studentInfoRepository.findAllByUserIn(listOf(active.user, withdrawn.user)))
            .thenReturn(listOf(active, withdrawn))
        whenever(attendancePlacementService.resolveRooms(listOf(active.user, withdrawn.user), date, checkpoint))
            .thenReturn(mapOf(active.user.id!! to room, withdrawn.user.id!! to room))

        service.generateExcelAndGetUrl(date)

        uploadedWorkbook().use { workbook ->
            val gradeSheet = workbook.getSheet("1학년")
            assertEquals(4, gradeSheet.physicalNumberOfRows)
            assertEquals(2.0, gradeSheet.getRow(3).getCell(0).numericCellValue)
            assertEquals("자퇴생", gradeSheet.getRow(3).getCell(1).stringCellValue)
            assertEquals("출석", gradeSheet.getRow(3).getCell(2).stringCellValue)

            val floorSheet = workbook.getSheet("1층")
            assertEquals(5, floorSheet.physicalNumberOfRows)
            assertEquals("1202", floorSheet.getRow(4).getCell(0).stringCellValue)
            assertEquals("자퇴생", floorSheet.getRow(4).getCell(1).stringCellValue)
            assertEquals("출석", floorSheet.getRow(4).getCell(2).stringCellValue)
        }
    }

    @Test
    fun `출석 기록이 없는 날짜에는 재학생 명단만 출력한다`() {
        val active = student(1L, "재학생", 1)
        stubExport(listOf(active), emptyList(), emptyList())
        whenever(attendancePlacementService.resolveRooms(listOf(active.user), date, checkpoint))
            .thenReturn(emptyMap())

        service.generateExcelAndGetUrl(date)

        verify(studentInfoRepository, never()).findAllByUserIn(any())
        uploadedWorkbook().use { workbook ->
            val sheet = workbook.getSheet("1학년")
            assertEquals(3, sheet.physicalNumberOfRows)
            assertEquals("재학생", sheet.getRow(2).getCell(1).stringCellValue)
            assertEquals(AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME, sheet.getRow(2).getCell(2).stringCellValue)
        }
    }

    @Test
    fun `미래 출석 Excel은 조회하거나 업로드하지 않는다`() {
        assertThrows(CustomException::class.java) {
            service.generateExcelAndGetUrl(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1))
        }

        verifyNoInteractions(s3Client, s3Presigner, attendanceRepository, studentInfoRepository)
    }

    private fun stubExport(
        activeStudents: List<StudentInfoEntity>,
        attendances: List<AttendanceEntity>,
        rooms: List<RoomEntity>,
    ) {
        whenever(checkpointRepository.findAllByIsDeletedFalse()).thenReturn(listOf(checkpoint))
        whenever(attendanceTypeRepository.findAllByIsDeletedFalse()).thenReturn(listOf(type))
        whenever(studentInfoRepository.findAllByUserIsDeletedFalse()).thenReturn(activeStudents)
        whenever(attendanceRepository.findAllByDate(date)).thenReturn(attendances)
        whenever(roomRepository.findAllByIsDeletedFalse()).thenReturn(rooms)
        whenever(roomApprovalRepository.findAllByCheckpointAndDate(checkpoint, date)).thenReturn(emptyList())
        val presignedRequest = mock<PresignedGetObjectRequest>()
        whenever(presignedRequest.url()).thenReturn(URL("https://example.com/attendance.xlsx"))
        whenever(s3Presigner.presignGetObject(any<GetObjectPresignRequest>())).thenReturn(presignedRequest)
    }

    private fun uploadedWorkbook(): XSSFWorkbook {
        val body = argumentCaptor<RequestBody>()
        verify(s3Client).putObject(any<PutObjectRequest>(), body.capture())
        return body.firstValue.contentStreamProvider().newStream().use { XSSFWorkbook(it) }
    }

    private fun student(id: Long, name: String, number: Int, isDeleted: Boolean = false): StudentInfoEntity {
        val user = UserEntity(
            id = id, username = "student-$id", name = name, role = UserRole.STUDENT, isDeleted = isDeleted
        )
        return StudentInfoEntity(id = id, user = user, grade = 1, classNumber = 2, num = number)
    }
}
