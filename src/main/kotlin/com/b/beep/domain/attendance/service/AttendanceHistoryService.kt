package com.b.beep.domain.attendance.service

import com.b.beep.domain.attendance.domain.entity.AttendanceEntity
import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.attendance.error.AttendanceError
import com.b.beep.domain.attendance.repository.AttendanceRepository
import com.b.beep.domain.attendance.repository.AttendanceTypeRepository
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.domain.checkpoint.repository.AttendanceCheckpointRepository
import com.b.beep.domain.room.domain.entity.RoomApprovalEntity
import com.b.beep.domain.room.domain.entity.RoomEntity
import com.b.beep.domain.room.repository.RoomApprovalRepository
import com.b.beep.domain.room.repository.RoomRepository
import com.b.beep.domain.user.domain.entity.StudentInfoEntity
import com.b.beep.domain.user.domain.entity.StudentScheduleEntity
import com.b.beep.domain.user.repository.StudentInfoRepository
import com.b.beep.domain.user.repository.StudentScheduleRepository
import com.b.beep.global.exception.CustomException
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class AttendanceHistoryService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val attendanceRepository: AttendanceRepository,
    private val attendanceTypeRepository: AttendanceTypeRepository,
    private val studentInfoRepository: StudentInfoRepository,
    private val studentScheduleRepository: StudentScheduleRepository,
    private val checkpointRepository: AttendanceCheckpointRepository,
    private val roomRepository: RoomRepository,
    private val roomApprovalRepository: RoomApprovalRepository,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String
) {
    fun listFiles(): List<String> {
        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix("uploads")
            .build()

        return s3Client.listObjectsV2(request).contents().map { it.key() }
    }

    fun generateExcelAndGetUrl(date: LocalDate): String {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        if (date.isAfter(today)) {
            throw CustomException(AttendanceError.FUTURE_DATE_NOT_ALLOWED)
        }

        val dayOfWeek = date.dayOfWeek
        val checkpoints = checkpointRepository.findAllByIsDeletedFalse().sortedBy { it.startAt }
        val attendanceTypes = attendanceTypeRepository.findAllByIsDeletedFalse().map { it.name }
        val allStudents = studentInfoRepository.findAllByUserIsDeletedFalse()
        val attendances = attendanceRepository.findAllByDate(date)
        val schedules = studentScheduleRepository.findAllByDayOfWeek(dayOfWeek)
        val rooms = roomRepository.findAllByIsDeletedFalse()
        val roomApprovals = checkpoints.flatMap { cp ->
            roomApprovalRepository.findAllByCheckpointAndDate(cp, date)
        }

        val bytes = createExcel(checkpoints, attendanceTypes, allStudents, attendances, schedules, rooms, roomApprovals)
        val key = "uploads/attendance_$date.xlsx"

        uploadToS3(key, bytes)

        return generatePresignedUrl(key)
    }

    private fun generatePresignedUrl(key: String, expiration: Duration = Duration.ofMinutes(10)): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    private fun createExcel(
        checkpoints: List<AttendanceCheckpointEntity>,
        attendanceTypes: List<String>,
        allStudents: List<StudentInfoEntity>,
        attendances: List<AttendanceEntity>,
        schedules: List<StudentScheduleEntity>,
        rooms: List<RoomEntity>,
        roomApprovals: List<RoomApprovalEntity>
    ): ByteArray {
        val workbook = XSSFWorkbook()
        val checkpointNames = checkpoints.map { it.name }
        val headerStyle = createCenteredStyle(workbook)

        createGradeSheets(workbook, allStudents, attendances, checkpoints, checkpointNames, attendanceTypes, headerStyle)
        createFloorSheets(workbook, attendances, schedules, allStudents, rooms, roomApprovals, checkpoints, checkpointNames, attendanceTypes, headerStyle)

        return ByteArrayOutputStream().use { out ->
            workbook.write(out)
            workbook.close()
            out.toByteArray()
        }
    }

    private fun createGradeSheets(
        workbook: XSSFWorkbook,
        allStudents: List<StudentInfoEntity>,
        attendances: List<AttendanceEntity>,
        checkpoints: List<AttendanceCheckpointEntity>,
        checkpointNames: List<String>,
        attendanceTypes: List<String>,
        headerStyle: XSSFCellStyle
    ) {
        val attendanceMap = attendances.groupBy { it.user.id }
        val colsPerClass = 2 + checkpointNames.size

        val studentDataList = allStudents.map { studentInfo ->
            val userAttendances = attendanceMap[studentInfo.user.id] ?: emptyList()
            val attendanceByCheckpoint = userAttendances.associateBy { it.checkpoint.id }

            GradeStudentData(
                grade = studentInfo.grade,
                classNumber = studentInfo.classNumber,
                num = studentInfo.num,
                name = studentInfo.user.username,
                statuses = checkpoints.map { cp ->
                    attendanceByCheckpoint[cp.id]?.type?.name ?: AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME
                }
            )
        }

        (1..3).forEach { grade ->
            val sheet = workbook.createSheet("${grade}학년")
            val gradeStudents = studentDataList.filter { it.grade == grade }
            val classesSorted = gradeStudents.groupBy { it.classNumber }.toSortedMap()

            var rowOffset = 0
            classesSorted.forEach { (classNumber, classStudents) ->
                val sortedStudents = classStudents.sortedBy { it.num }
                createClassHeader(sheet, classNumber, checkpointNames, colsPerClass, headerStyle, rowOffset)
                fillClassStudents(sheet, sortedStudents, rowOffset)
                addDropdownValidation(sheet, sortedStudents.size, checkpointNames, attendanceTypes, rowOffset)
                rowOffset += sortedStudents.size + 3
            }
            autoSizeColumns(sheet, colsPerClass)
        }
    }

    private fun createClassHeader(
        sheet: XSSFSheet,
        classNumber: Int,
        checkpointNames: List<String>,
        colsPerClass: Int,
        headerStyle: XSSFCellStyle,
        rowOffset: Int
    ) {
        val classHeaderRow = sheet.createRow(rowOffset)
        classHeaderRow.createCell(0).apply {
            setCellValue("${classNumber}반")
            cellStyle = headerStyle
        }
        sheet.addMergedRegion(CellRangeAddress(rowOffset, rowOffset, 0, colsPerClass - 1))

        val colHeaderRow = sheet.createRow(rowOffset + 1)
        val headers = listOf("번호", "이름") + checkpointNames
        headers.forEachIndexed { index, header ->
            colHeaderRow.createCell(index).setCellValue(header)
        }
    }

    private fun fillClassStudents(
        sheet: XSSFSheet,
        sortedStudents: List<GradeStudentData>,
        rowOffset: Int
    ) {
        sortedStudents.forEachIndexed { index, student ->
            val row = sheet.createRow(rowOffset + 2 + index)
            row.createCell(0).setCellValue(student.num.toDouble())
            row.createCell(1).setCellValue(student.name)
            student.statuses.forEachIndexed { statusIndex, status ->
                row.createCell(2 + statusIndex).setCellValue(status)
            }
        }
    }

    private fun createFloorSheets(
        workbook: XSSFWorkbook,
        attendances: List<AttendanceEntity>,
        schedules: List<StudentScheduleEntity>,
        allStudents: List<StudentInfoEntity>,
        rooms: List<RoomEntity>,
        roomApprovals: List<RoomApprovalEntity>,
        checkpoints: List<AttendanceCheckpointEntity>,
        checkpointNames: List<String>,
        attendanceTypes: List<String>,
        headerStyle: XSSFCellStyle
    ) {
        val colsPerRoom = 2 + checkpointNames.size
        val approvalMap = roomApprovals.groupBy { it.room.id to it.checkpoint.id }
        val attendanceByUser = attendances.groupBy { it.user.id }
        val schedulesByRoom = schedules.groupBy { it.room.id }
        val studentInfoByUser = allStudents.associateBy { it.user.id }

        (1..3).forEach { floor ->
            val sheet = workbook.createSheet("${floor}층")
            val floorRooms = rooms.filter { it.floor == floor }.sortedBy { it.name }

            if (floorRooms.isEmpty()) return@forEach

            var rowOffset = 0
            floorRooms.forEach { room ->
                val students = buildRoomStudents(room, schedulesByRoom, attendanceByUser, studentInfoByUser, checkpoints)
                createRoomHeader(sheet, room, checkpoints, checkpointNames, colsPerRoom, approvalMap, headerStyle, rowOffset)
                fillRoomStudents(sheet, students, rowOffset)
                addRoomDropdownValidation(sheet, students.size, checkpointNames, attendanceTypes, rowOffset)
                rowOffset += students.size + 4
            }
            autoSizeColumns(sheet, colsPerRoom)
        }
    }

    private fun buildRoomStudents(
        room: RoomEntity,
        schedulesByRoom: Map<Long?, List<StudentScheduleEntity>>,
        attendanceByUser: Map<Long?, List<AttendanceEntity>>,
        studentInfoByUser: Map<Long?, StudentInfoEntity>,
        checkpoints: List<AttendanceCheckpointEntity>
    ): List<RoomStudentData> {
        val roomSchedules = schedulesByRoom[room.id] ?: emptyList()
        val userIds = roomSchedules.map { it.user.id }.distinct()

        return userIds.mapNotNull { userId ->
            val userSchedules = roomSchedules.filter { it.user.id == userId }
            val user = userSchedules.firstOrNull()?.user ?: return@mapNotNull null
            val studentInfo = studentInfoByUser[userId] ?: return@mapNotNull null
            val userAttendances = attendanceByUser[userId] ?: emptyList()
            val attendanceByCheckpoint = userAttendances.associateBy { it.checkpoint.id }
            val userScheduleByCheckpoint = userSchedules.associateBy { it.checkpoint.id }

            val studentNumber =
                "${studentInfo.grade}${studentInfo.classNumber}${String.format("%02d", studentInfo.num)}"

            RoomStudentData(
                studentNumber = studentNumber,
                name = user.username,
                statuses = checkpoints.map { cp ->
                    if (userScheduleByCheckpoint[cp.id] == null) {
                        "--"
                    } else {
                        attendanceByCheckpoint[cp.id]?.type?.name ?: AttendanceTypeEntity.NOT_ATTENDED_TYPE_NAME
                    }
                }
            )
        }.sortedBy { it.studentNumber }
    }

    private fun createRoomHeader(
        sheet: XSSFSheet,
        room: RoomEntity,
        checkpoints: List<AttendanceCheckpointEntity>,
        checkpointNames: List<String>,
        colsPerRoom: Int,
        approvalMap: Map<Pair<Long?, Long?>, List<RoomApprovalEntity>>,
        headerStyle: XSSFCellStyle,
        rowOffset: Int
    ) {
        val roomHeaderRow = sheet.createRow(rowOffset)
        roomHeaderRow.createCell(0).apply {
            setCellValue(room.name)
            cellStyle = headerStyle
        }
        sheet.addMergedRegion(CellRangeAddress(rowOffset, rowOffset, 0, colsPerRoom - 1))

        val approvalHeaderRow = sheet.createRow(rowOffset + 1)
        approvalHeaderRow.createCell(0).setCellValue("")
        approvalHeaderRow.createCell(1).setCellValue("")
        checkpoints.forEachIndexed { cpIndex, cp ->
            val approval = approvalMap[room.id to cp.id]?.firstOrNull()
            val approvalText = if (approval != null) {
                val time = approval.createdAt?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                val teacher = approval.teacher?.username ?: "-"
                "승인 $time $teacher"
            } else "미승인"
            approvalHeaderRow.createCell(2 + cpIndex).setCellValue(approvalText)
        }

        val colHeaderRow = sheet.createRow(rowOffset + 2)
        val headers = listOf("학번", "이름") + checkpointNames
        headers.forEachIndexed { index, header ->
            colHeaderRow.createCell(index).setCellValue(header)
        }
    }

    private fun fillRoomStudents(
        sheet: XSSFSheet,
        students: List<RoomStudentData>,
        rowOffset: Int
    ) {
        students.forEachIndexed { index, student ->
            val row = sheet.createRow(rowOffset + 3 + index)
            row.createCell(0).setCellValue(student.studentNumber)
            row.createCell(1).setCellValue(student.name)
            student.statuses.forEachIndexed { statusIndex, status ->
                row.createCell(2 + statusIndex).setCellValue(status)
            }
        }
    }

    private fun addDropdownValidation(
        sheet: XSSFSheet,
        studentCount: Int,
        checkpointNames: List<String>,
        attendanceTypes: List<String>,
        rowOffset: Int
    ) {
        if (studentCount == 0) return
        val helper = sheet.dataValidationHelper
        val constraint = helper.createExplicitListConstraint(attendanceTypes.toTypedArray())
        val startRow = rowOffset + 2
        val endRow = rowOffset + 1 + studentCount

        for (cpIndex in checkpointNames.indices) {
            val statusCol = 2 + cpIndex
            val addressList = CellRangeAddressList(startRow, endRow, statusCol, statusCol)
            val validation = helper.createValidation(constraint, addressList)
            validation.showErrorBox = true
            sheet.addValidationData(validation)
        }
    }

    private fun addRoomDropdownValidation(
        sheet: XSSFSheet,
        studentCount: Int,
        checkpointNames: List<String>,
        attendanceTypes: List<String>,
        rowOffset: Int
    ) {
        if (studentCount == 0) return
        val helper = sheet.dataValidationHelper
        val constraint = helper.createExplicitListConstraint(attendanceTypes.toTypedArray())
        val startRow = rowOffset + 3
        val endRow = rowOffset + 2 + studentCount

        for (cpIndex in checkpointNames.indices) {
            val statusCol = 2 + cpIndex
            val addressList = CellRangeAddressList(startRow, endRow, statusCol, statusCol)
            val validation = helper.createValidation(constraint, addressList)
            validation.showErrorBox = true
            sheet.addValidationData(validation)
        }
    }

    private fun createCenteredStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
        }
    }

    private fun autoSizeColumns(sheet: XSSFSheet, totalCols: Int) {
        val minWidth = 256 * 10
        (0 until totalCols).forEach { col ->
            sheet.autoSizeColumn(col)
            if (sheet.getColumnWidth(col) < minWidth) {
                sheet.setColumnWidth(col, minWidth)
            }
        }
    }

    private fun uploadToS3(key: String, bytes: ByteArray) {
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .build()
        s3Client.putObject(request, RequestBody.fromBytes(bytes))
    }

    private data class GradeStudentData(
        val grade: Int,
        val classNumber: Int,
        val num: Int,
        val name: String,
        val statuses: List<String>
    )

    private data class RoomStudentData(
        val studentNumber: String,
        val name: String,
        val statuses: List<String>
    )
}