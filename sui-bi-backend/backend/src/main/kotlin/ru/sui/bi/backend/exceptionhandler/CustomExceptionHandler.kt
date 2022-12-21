package ru.sui.bi.backend.exceptionhandler

import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.WebUtils
import ru.sui.bi.backend.dto.ErrorDto
import java.time.Instant

private val log = KotlinLogging.logger { }

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    fun handleAll(exception: Exception, request: WebRequest): ResponseEntity<*>? {
        log.debug(exception) { "Exception while processing request" }

        return try {
            handleException(exception, request)
        } catch (ex: Exception) {
            handleExceptionInternal(
                ex = exception,
                body = null,
                headers = HttpHeaders(),
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                request = request
            )
        }
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST)
        }

        val errorDto = ErrorDto(
            timestamp = Instant.now(),
            status = status.reasonPhrase,
            statusCode = status.value(),
            message = ex.message ?: ex::class.java.canonicalName,
            details = ex.stackTraceToString()
        )

        return ResponseEntity.status(status).headers(headers).body(errorDto)
    }

}