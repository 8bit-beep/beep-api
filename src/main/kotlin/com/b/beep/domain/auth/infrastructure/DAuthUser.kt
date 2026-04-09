package com.b.beep.domain.auth.infrastructure

data class DAuthUser(
    val publicId: String,                    // sub (사용자 고유 ID)
    val username: String,         // 로그인 아이디
    val name: String,         // 이름
    val phone: String? = null,  // 전화번호
    val profileImage: String? = null,  // 프로필 이미지
    val status: String,
    val roles: List<String>,          // 역할 (STUDENT, TEACHER 등)
    val student: StudentInfo? = null,  // 학생 정보
    val teacher: TeacherInfo? = null,  // 교사 정보
    val createdAt: String? = null     // 계정 생성일
)
