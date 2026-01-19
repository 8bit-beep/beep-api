package com.b.beep.domain.absence.domain.entity

import com.b.beep.domain.attendance.domain.entity.AttendanceTypeEntity
import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import jakarta.persistence.*

@Entity
@Table(name = "absence_checkpoints")
class AbsenceCheckpointEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id")
    val checkpoint: AttendanceCheckpointEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_id")
    val absence: AbsenceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    val type: AttendanceTypeEntity? = null
)
