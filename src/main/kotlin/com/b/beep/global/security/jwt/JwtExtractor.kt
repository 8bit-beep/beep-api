package com.b.beep.global.security.jwt

import com.b.beep.domain.user.repository.UserRepository
import com.b.beep.global.security.auth.AuthDetails
import com.b.beep.global.security.jwt.config.JwtProperties
import com.b.beep.global.security.jwt.enums.JwtType
import com.b.beep.global.security.jwt.error.JwtError
import com.b.beep.global.exception.CustomException
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import org.slf4j.LoggerFactory

@Component
class JwtExtractor(
    private val jwtProperties: JwtProperties,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(JwtExtractor::class.java)

    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun getUsername(token: String): String = getClaims(token).body.subject.trim()

    fun getAuthentication(token: String): Authentication {
        val claims = getClaims(token).body
        val subject = claims.subject.trim()
        log.info("[JWT] subject='{}'", subject)
        val user = userRepository.findByUsernameAndIsDeletedFalse(subject) ?:
        userRepository.findByPublicIdAndIsDeletedFalse(subject) ?:
        throw CustomException(JwtError.INVALID_TOKEN, subject)
        val details = AuthDetails(user)

        return UsernamePasswordAuthenticationToken(details, null, details.authorities)
    }

    fun extractToken(request: HttpServletRequest) =
        request.getHeader(jwtProperties.header)?.removePrefix(jwtProperties.prefix)

    private fun getClaims(token: String): Jws<Claims> {
        try {
            return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
        } catch (e: ExpiredJwtException) {
            throw CustomException(JwtError.EXPIRED_TOKEN)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(JwtError.UNSUPPORTED_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw CustomException(JwtError.INVALID_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(JwtError.MALFORMED_TOKEN)
        }
    }

    fun validateTokenType(token: String, type: JwtType): Boolean {
        val claims = getClaims(token)

        return (claims.header.equals(type))
    }
}