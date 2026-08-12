package com.b.beep.domain.auth.service

import com.b.beep.global.properties.DomainProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class OAuth2FailureHandler(
    private val domainProperties: DomainProperties
) : AuthenticationFailureHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val oauthErrorCode = (exception as? OAuth2AuthenticationException)?.error?.errorCode
        val reason = when (oauthErrorCode) {
            "authorization_request_not_found", "invalid_state_parameter" -> SESSION_EXPIRED
            "access_denied" -> ACCESS_DENIED
            else -> OAUTH_FAILED
        }
        val requestedSessionIdPresent = request.requestedSessionId != null

        logger.warn(
            "OAuth2 login failed: reason={}, oauthErrorCode={}, exceptionType={}, " +
                "sessionPresent={}, requestedSessionIdPresent={}, requestedSessionIdValid={}",
            reason,
            oauthErrorCode ?: "unknown",
            exception.javaClass.simpleName,
            request.getSession(false) != null,
            requestedSessionIdPresent,
            requestedSessionIdPresent && request.isRequestedSessionIdValid
        )

        response.sendRedirect(
            "${domainProperties.web.trimEnd('/')}/login?error=oauth&reason=$reason"
        )
    }

    private companion object {
        const val SESSION_EXPIRED = "session_expired"
        const val ACCESS_DENIED = "access_denied"
        const val OAUTH_FAILED = "oauth_failed"
    }
}
