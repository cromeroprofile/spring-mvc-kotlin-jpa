package com.example.kotlinmvc

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional


@SpringBootTest
@Transactional
class MessageServiceImplTest {

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var messageService: MessageService

    @Test
    fun `should create message with uppercase text`() {
        val messageCreate = MessageCreate(text = "test")

        var findAll = messageRepository.findAll()
        assert(findAll.isEmpty())

        val savedMessage = messageService.save(messageCreate)

        findAll = messageRepository.findAll()
        assert(findAll.isNotEmpty() && findAll.size == 1)
        assert(findAll.any { it.text == "TEST" })
        assert(savedMessage.text == "TEST")

    }


    @Test
    fun `should findAll `() {
        val messageModel = MessageModel(text = "test")

        messageRepository.save(messageModel)
        assert(messageRepository.count() == 1L)

        var findAll = messageService.findAll(null)
        assert(findAll.isNotEmpty() && findAll.size == 1)
        assert(findAll.any { it.text == "test" })

        findAll = messageService.findAll("test")
        assert(findAll.isEmpty())

    }


}
