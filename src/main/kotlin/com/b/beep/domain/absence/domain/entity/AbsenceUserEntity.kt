package com.b.beep.domain.absence.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import jakarta.persistence.*

@Entity
@Table(name = "absence_users")
class AbsenceUserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "absence_id")
    val absence: AbsenceEntity
)
