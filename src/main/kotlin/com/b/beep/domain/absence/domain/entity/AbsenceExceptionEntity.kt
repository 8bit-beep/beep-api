package com.b.beep.domain.absence.domain.entity

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "absence_exceptions")
class AbsenceExceptionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_id", nullable = false)
    val absence: AbsenceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    val checkpoint: AttendanceCheckpointEntity,

    @Column(nullable = false)
    val date: LocalDate
) : BaseEntity()
