package com.b.beep.global.exception

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

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ErrorResponse.of(CustomException(GlobalError.INTERNAL_SERVER_ERROR, e.message.toString()))
    }
}