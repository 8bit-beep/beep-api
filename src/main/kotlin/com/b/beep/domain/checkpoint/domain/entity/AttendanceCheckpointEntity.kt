package com.b.beep.domain.checkpoint.domain.entity

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "attendance_checkpoints")
class AttendanceCheckpointEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @Column(name = "start_at", nullable = false)
    var startAt: LocalTime,

    @Column(name = "end_at", nullable = false)
    var endAt: LocalTime,

    @Column(name = "attendance_start_at")
    var attendanceStartAt: LocalTime,

    @Column(name = "attendance_end_at")
    var attendanceEndAt: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = true)
    var dayOfWeek: DayOfWeek? = null,

    @Column(name = "grade", nullable = true)
    var grade: Int? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false
)
