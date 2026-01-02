//package com.b.beep.domain.auth.service
//
//import com.b.beep.domain.auth.domain.AuthError
//import com.b.beep.domain.auth.infrastructure.DAuthUser
//import com.b.beep.domain.user.domain.UserRole
//import com.b.beep.domain.user.repository.UserRepository
//import com.b.beep.domain.user.service.StudentInfoService
//import com.b.beep.global.exception.CustomException
//import com.b.beep.global.security.jwt.JwtProvider
//import org.springframework.security.core.Authentication
//import jakarta.servlet.http.HttpServletRequest
//import jakarta.servlet.http.HttpServletResponse
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.oauth2.core.user.OAuth2User
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler
//import org.springframework.stereotype.Component
//
//@Component
//class OAuth2SuccessHandler(
//    private val studentInfoService: StudentInfoService,
//    private val jwtProvider: JwtProvider
//) : AuthenticationSuccessHandler {
//
//    override fun onAuthenticationSuccess(
//        request: HttpServletRequest,
//        response: HttpServletResponse,
//        authentication: Authentication
//    ){
//        val oauth2User = authentication.principal as OAuth2User
//        val attributes = oauth2User.attributes
//
//        val email = attributes["email"] as? String
//            ?: throw CustomException(AuthError.NULL_EMAIL)
//        val name = attributes["name"] as? String
//            ?:throw CustomException(AuthError.NULL_NAME)
//
//        val dauthUser = DAuthUser(
//            id = attributes["sub"] as String,
//            name = name,
//            email = email,
//            profileImage = attributes["profile_image"] as? String,
//            role = attributes["role"] as? String,
//            phone = attributes["phone"] as? String
//        )
//
//        val user = studentInfoService.getOrCreateUser(dauthUser)
//
//        if (user.role == UserRole.STUDENT) {
//            studentInfoService.getOrCreateStudentInfo(user, dauthUser)
//        }
//
////        return jwtProvider.generateToken(user.email)
//
//        SecurityContextHolder.clearContext()
//    }
//}
