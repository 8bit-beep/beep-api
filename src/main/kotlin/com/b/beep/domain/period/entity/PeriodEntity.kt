package com.b.beep.domain.period.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "periods")
class PeriodEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "period", nullable = false, unique = true)
    val period: Int,

    @Column(name = "attendance_start_time", nullable = false)
    var attendanceStartTime: LocalTime,

    @Column(name = "attendance_end_time", nullable = false)
    var attendanceEndTime: LocalTime,

    @Column(name = "period_start_time", nullable = false)
    var periodStartTime: LocalTime,

    @Column(name = "period_end_time", nullable = false)
    var periodEndTime: LocalTime,
)
