package com.b.beep.domain.user.domain.entity

import com.b.beep.domain.user.domain.enums.UserRole
import com.b.beep.global.common.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "public_id", unique = true)
    var publicId: String? = null,

    @Column(name = "username", nullable = false, unique = true)
    var username: String,  // 로그인 아이디

    @Column(name = "name", nullable = false)
    var name: String,  // 진짜 이름

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