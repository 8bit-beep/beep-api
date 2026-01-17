package com.b.beep.global.security.auth

import com.b.beep.domain.user.domain.entity.UserEntity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthDetails(private val user: UserEntity) : UserDetails {
    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return user.email
    }
}