package com.b.beep.domain.absence.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "absences")
class AbsenceEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var reason: String
)
