package com.b.beep.domain.user.domain.entity

import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.global.common.entity.BaseEntity
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
    var profileImage: String? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Column(nullable = true)
    val password: String? = null

) : BaseEntity()