package com.b.beep.domain.absence.domain.entity

import com.b.beep.domain.absence.domain.AbsenceReason
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "absences")
class AbsenceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var reason: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var absenceType: AbsenceReason,

    @Column(nullable = false)
    var isDeleted: Boolean = false
) : BaseEntity()
