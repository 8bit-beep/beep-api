package com.b.beep.global.exception

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.MethodNotAllowedException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException) = ErrorResponse.of(e)

    @ExceptionHandler(MethodNotAllowedException::class)
    fun handleMethodNotAllowedException(e: MethodNotAllowedException) =
        ErrorResponse.of(CustomException(GlobalError.METHOD_NOT_ALLOWED))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException) =
        ErrorResponse.of(CustomException(GlobalError.METHOD_NOT_ALLOWED))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException) =
        ErrorResponse.of(CustomException(GlobalError.HTTP_MESSAGE_NOT_READABLE))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException) =
        ErrorResponse.of(CustomException(GlobalError.METHOD_ARGUMENT_TYPE_MISMATCH))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "검증 오류가 발생했습니다."
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "METHOD_ARGUMENT_NOT_VALID",
                status = 400,
                message = message
            )
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val message = e.constraintViolations.firstOrNull()?.message
            ?: "검증 오류가 발생했습니다."
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "CONSTRAINT_VIOLATION",
                status = 400,
                message = message
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled server exception", e)
        return ErrorResponse.of(CustomException(GlobalError.INTERNAL_SERVER_ERROR))
    }
}
