package com.b.beep.domain.event.domain.entity

import com.b.beep.domain.user.domain.entity.UserEntity
import jakarta.persistence.*

@Entity
@Table(name = "event_users")
class EventUserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: EventEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity
)
