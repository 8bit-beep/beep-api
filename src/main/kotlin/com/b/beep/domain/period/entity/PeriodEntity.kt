package com.b.beep.domain.period.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "periods")
class PeriodEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val period: Int,

    var startTime: LocalTime,
    var endTime: LocalTime,
    var attendStartTime: LocalTime,
    var attendEndTime: LocalTime
)
