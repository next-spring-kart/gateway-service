package com.nextspringkart.gatewayservice.exception

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

@RestControllerAdvice
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper,
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        val dataBufferFactory: DataBufferFactory = response.bufferFactory()

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "An unexpected error occurred",
            path = exchange.request.uri.path
        )

        response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        response.headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)

        logger.error("Gateway error: ${ex.message}", ex)

        return try {
            val json = objectMapper.writeValueAsString(errorResponse)
            val dataBuffer = dataBufferFactory.wrap(json.toByteArray())
            response.writeWith(Mono.just(dataBuffer))
        } catch (e: JsonProcessingException) {
            logger.error("Error writing response", e)
            response.setComplete()
        }
    }
}
