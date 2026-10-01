package com.b.beep.domain.attendance.domain.entity

import com.b.beep.domain.checkpoint.domain.entity.AttendanceCheckpointEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.DayOfWeek

@Entity
@Table(
    name = "attendance_sort_mode_defaults",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_sort_mode_default",
            columnNames = ["day_of_week", "checkpoint_id", "grade"]
        )
    ]
)
class AttendanceSortModeDefaultEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    val dayOfWeek: DayOfWeek,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    val checkpoint: AttendanceCheckpointEntity,

    @Column(nullable = false)
    val grade: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    var type: AttendanceTypeEntity
) : BaseEntity()
