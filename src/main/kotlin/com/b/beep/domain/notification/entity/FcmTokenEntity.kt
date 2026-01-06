package com.b.beep.domain.notification.entity

import com.b.beep.domain.user.entity.UserEntity
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "fcm_tokens",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])]
)
class FcmTokenEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "token", nullable = false)
    var token: String,

    @Column(name = "device", nullable = false)
    val device: String
) : BaseEntity()
