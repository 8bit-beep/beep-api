package com.b.beep.domain.auth.infrastructure

data class DAuthUser(
    val id: String,                    // sub (사용자 고유 ID)
    val name: String,         // 이름
    val email: String,         // 이메일
    val profileImage: String?  = null,  // 프로필 이미지
    val role: String?  = null,          // 역할 (STUDENT, TEACHER 등)
    val phone: String? = null,  // 전화번호
    val grade: Int?,
    val number: Int? ,
    val room: Int? ,
//    val clubs: List<String> //enum 변경
)
