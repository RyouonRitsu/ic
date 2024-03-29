package com.ryouonritsu.ic.config

import com.ryouonritsu.ic.common.enums.ExceptionEnum
import com.ryouonritsu.ic.common.exception.ServiceException
import com.ryouonritsu.ic.domain.protocol.response.Response
import org.slf4j.LoggerFactory
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * @author ryouonritsu
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(value = [ServiceException::class])
    fun serviceExceptionHandler(serviceException: ServiceException): Response<Unit> {
        log.error(
            "ServiceException occurred: code = ${serviceException.code}, message = ${serviceException.message}",
            serviceException
        )
        return Response.failure(serviceException)
    }

    @ExceptionHandler(value = [NullPointerException::class])
    fun exceptionHandler(exception: NullPointerException): Response<Unit> {
        log.error("NullPointerException occurred: ${exception.cause}", exception)
        return Response.failure(exception.message.toString())
    }

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun exceptionHandler(exception: MethodArgumentNotValidException): Response<Map<String, String?>> {
        log.error("MethodArgumentNotValidException occurred: ${exception.cause}", exception)
        return Response.failure(ExceptionEnum.BAD_REQUEST, ExceptionEnum.BAD_REQUEST.message,
            exception.bindingResult.allErrors.associate { (it as FieldError).field to it.defaultMessage })
    }

    @ExceptionHandler(value = [Exception::class])
    fun exceptionHandler(exception: Exception): Response<Unit> {
        log.error("UnknownError occurred: ${exception.cause}", exception)
        return Response.failure(ExceptionEnum.INTERNAL_SERVER_ERROR, exception.message.toString())
    }
}