package com.b.beep.domain.user.entity

import com.b.beep.global.common.entity.BaseEntity
import com.b.beep.domain.user.domain.UserRole
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "username", nullable = false)
    var username: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole,

    @Column(name = "profile_image", nullable = true)
    var profileImage: String? = null
) : BaseEntity()
