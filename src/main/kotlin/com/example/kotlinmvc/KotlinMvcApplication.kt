package com.example.kotlinmvc

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.lang.NullPointerException
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}


    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal Server Error")
    @ExceptionHandler(RuntimeException::class)
    fun customerNotFound(exception: RuntimeException) {
        logger.error (exception){ "Exception" }
    }
}

@Configuration
class MyConfig {
    @Bean
    fun otpCounterOk(registry: MeterRegistry): Counter = registry.counter("otp_ok")

    @Bean
    fun otpCounterKo(registry: MeterRegistry): Counter = registry.counter("otp_ko")
}


// controller

@RestController
@RequestMapping("/message")
class MessageController(val meterRegistry: MeterRegistry, val otpCounterOk: Counter,val otpCounterKo: Counter ) {

    private val logger = KotlinLogging.logger {}

    @GetMapping( "/ok")
    fun otpOk(): String {
        logger.info { "Otp ok" }
        otpCounterOk.increment()
        return  otpCounterOk.count().toString()
    }
    @GetMapping( "/ko")
    fun otpFail(
    ): String {
        logger.info { "Otp ko" }
        otpCounterKo.increment()
        return  otpCounterKo.count().toString()
    }


    @GetMapping( "/exception")
    fun exception(
    ): String {
        logger.info { "Otp Exception" }
        throw NullPointerException("crash")
        return  otpCounterKo.count().toString()
    }
}


@SpringBootApplication
class KotlinWebApplication

fun main(args: Array<String>) {
    runApplication<KotlinWebApplication>(*args)
}

