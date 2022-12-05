package com.example.apigateway.exception

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
class LoginExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler{
    private val log = LoggerFactory.getLogger(LoginExceptionHandler::class.java)
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> = mono{

        log.error ("${ex.message}")

        val errorResponse = if (ex is CustomException) {
            ErrorResponse(code = ex.code, message = ex.message)
        } else {
            ErrorResponse(code = 500, message = "Internal Server Error")
        }

        with(exchange.response) {
            this.statusCode = HttpStatus.OK
            headers.contentType = MediaType.APPLICATION_JSON

            val dataBuffer = bufferFactory().wrap(objectMapper.writeValueAsBytes(errorResponse))
            writeWith(dataBuffer.toMono()).awaitSingle()
        }
    }


}