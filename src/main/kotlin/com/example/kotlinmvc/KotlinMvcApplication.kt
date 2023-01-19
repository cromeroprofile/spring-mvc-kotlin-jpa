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


    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Customer could not be found!")
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

// data classes

@Entity
data class MessageModel(
    @Id @GeneratedValue var id: Long? = null,
    @Column(nullable = false)
    val text: String = ""
)

data class MessageOut(
    @field:Schema(description = "message id", example = "2")
    val id: Long,
    @field:Schema(description = "text ", minLength = 1, maxLength = 100, example = "ppp")
    val text: String
)

data class MessageCreate(
    @field:Schema(description = "text ", minLength = 1, maxLength = 100, example = "ppp")
    val text: String?
)

// layer's mappers

fun MessageCreate.convertToMessageModel() = MessageModel(
    text = this.text?.uppercase() ?: "emptyText"
)

fun MessageModel.covertToMessage() = MessageOut(
    id = this.id ?: -1,
    text = this.text
)

// controller

@RestController
@RequestMapping("/message")
class MessageController(val messageService: MessageService, val meterRegistry: MeterRegistry, val otpCounterOk: Counter,val otpCounterKo: Counter ) {



    private val logger = KotlinLogging.logger {}

    @Operation(summary = "Get all messages - exclude result if excludedText is provided")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Get all messages", content = [
                    (Content(
                        mediaType = "application/json", array = (
                                ArraySchema(schema = Schema(implementation = MessageOut::class)))
                    ))]
            ),
            ApiResponse(responseCode = "400", description = "bad request", content = [Content()]),
            ApiResponse(responseCode = "404", description = "not found", content = [Content()])]
    )
    @GetMapping
    fun findAll(
        @Parameter(description = "Get all messages do not include the text")
        @RequestParam excludeText: String?
    ): List<MessageOut> {
        return messageService.findAll(excludeText)
    }


    @GetMapping( "/ok")
    fun otpOk(
    ): String {
        logger.info { "pasa ok otp 2" }
        otpCounterOk.increment()
        return  otpCounterOk.count().toString()
    }
    @GetMapping( "/ko")
    fun otpFail(
    ): String {
        logger.info { "pasa ok" }
        otpCounterKo.increment()
        return  otpCounterKo.count().toString()
    }


    @GetMapping( "/exception")
    fun exception(
    ): String {
        logger.info { "pasa exception" }
        throw NullPointerException("peteee")
        return  otpCounterKo.count().toString()
    }


    @Operation(summary = "Create a messageOut")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Create a message",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = MessageOut::class))]
            ),
            ApiResponse(responseCode = "400", description = "bad request", content = [Content()]),
            ApiResponse(responseCode = "404", description = "not found", content = [Content()])]
    )
    @PostMapping
    fun save(@RequestBody messageCreate: MessageCreate): MessageOut = messageService.save(messageCreate)

}

// service

interface MessageService {
    fun save(messageOut: MessageCreate): MessageOut
    fun findAll(excludeText: String?): List<MessageOut>
}


@Service
class MessageServiceImpl(val messageRepository: MessageRepository) : MessageService {

    override fun save(messageCreate: MessageCreate): MessageOut {
        val save = messageRepository.save(messageCreate.convertToMessageModel())
        return save.covertToMessage()
    }

    override fun findAll(excludeText: String?): List<MessageOut> {

        // same behaviour using kotlin's nullability features let and elvis operator
        // return excludeText?.let { text -> messageRepository.findByTextNot(text).map { it.covertToMessage() } }
        //     ?:  messageRepository.findAll().map { it.covertToMessage() }

        return if (excludeText != null) {
            messageRepository.findByTextNot(excludeText).map { it.covertToMessage() }
        } else {
            messageRepository.findAll().map { it.covertToMessage() }
        }
    }

}

// repository
@Repository
interface MessageRepository : JpaRepository<MessageModel, Long> {
    fun findByTextNot(text: String): List<MessageModel>
}


@SpringBootApplication
class KotlinWebApplication

fun main(args: Array<String>) {
    runApplication<KotlinWebApplication>(*args)
}

