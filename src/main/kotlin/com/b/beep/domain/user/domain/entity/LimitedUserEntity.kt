package com.b.beep.domain.user.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "limited_users")
class LimitedUserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "usrename", nullable = false, unique = true)
    var username: String,
)