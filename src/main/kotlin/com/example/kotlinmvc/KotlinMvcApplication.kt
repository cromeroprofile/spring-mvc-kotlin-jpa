package com.example.kotlinmvc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@Entity
data class MessageModel(
    @Id @GeneratedValue var id: Long? = null,
    @Column(nullable = false)
    var text: String = ""
)

data class Message(
    @field:Schema(description = "message id", example = "2")
    val id: Long? = null,
    @field:Schema(description = "text ", minLength = 1 , maxLength = 100, example = "ppp")
    val text: String?)


fun Message.covertToMessageModel() = MessageModel(
    id = this.id ?: -1,
    text = this.text?.uppercase() ?: "emptyText"
)

fun MessageModel.covertToMessage() = Message(
    id = this.id ?: -1,
    text = this.text
)





@RestController
@RequestMapping("/message")
class MessageController(val messageService: MessageService) {

    @Operation(summary = "Get all messages")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Get all messages", content = [
            (Content(mediaType = "application/json", array = (
                    ArraySchema(schema = Schema(implementation = Message::class)))))]),
        ApiResponse(responseCode = "400", description = "bad request", content = [Content()]),
        ApiResponse(responseCode = "404", description = "not found", content = [Content()])])
    @GetMapping
    fun findAll(@RequestParam text: String?): List<Message> = messageService.findAll(text)


    @Operation(summary = "Create a message")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Create a message",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Message::class))]
        ),
        ApiResponse(responseCode = "400", description = "bad request", content = [Content()]),
        ApiResponse(responseCode = "404", description = "not found", content = [Content()])]
    )
    @PostMapping
    fun save(@RequestBody message: Message): Message = messageService.save(message)

}

interface MessageService {
    fun save(message: Message): Message
    fun findAll(text: String?): List<Message>
}

@Service
class MessageServiceImpl(val messageRepository: MessageRepository) : MessageService {

    override fun save(message: Message): Message {
        val save = messageRepository.save(message.covertToMessageModel())
        return save.covertToMessage()
    }

    override fun findAll(text: String?): List<Message> {

//        text?.let { text -> messageRepository.findByTextNot(text).map { it.covertToMessage() } }
//        ? : messageRepository.findAll().map { it.covertToMessage() }

        return if (text!=null)
        {
            messageRepository.findByTextNot(text).map { it.covertToMessage() }
        }
        else
        {
            messageRepository.findAll().map { it.covertToMessage() }
        }
    }


}

interface MessageRepository : JpaRepository<MessageModel, Long> {
    fun findByTextNot(text: String): List<MessageModel>
}


@SpringBootApplication
class KotlinWebApplication

fun main(args: Array<String>) {
    runApplication<KotlinWebApplication>(*args)
}

