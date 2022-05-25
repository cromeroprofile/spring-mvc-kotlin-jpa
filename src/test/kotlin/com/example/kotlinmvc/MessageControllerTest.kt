package com.example.kotlinmvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post


@WebMvcTest
class MessageControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var messageService: MessageService

    @Autowired
    lateinit var mapper: ObjectMapper

    @Test
    fun `should create message with uppercase text`() {

        val messageCreate = MessageCreate(text = "ing")

        `when`(messageService.save(messageCreate)).thenReturn(MessageOut(1, "ING"))

        mockMvc.perform(
            post("/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(messageCreate))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.text").value("ING"))

    }


    @Test
    fun `should findAll `() {

        `when`(messageService.findAll(null)).thenReturn(listOf(MessageOut(1, "ING")))

        mockMvc.perform(get("/message"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].text").value("ING"))


    }





    @Test
    fun `should findAll using kotlin dsl `() {

        `when`(messageService.findAll("text")).thenReturn(listOf(MessageOut(1, "ING")))

        mockMvc.get("/message") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            param("excludeText","text")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.[0].id") { value("1") }
            jsonPath("$.[0].text") { value("ING") }

        }
    }

    @Test
    fun `should create message with uppercase text using kotlin dsl`() {

        val messageCreate = MessageCreate(text = "ing")

        val messageOut = MessageOut(1, "ING")
        `when`(messageService.save(messageCreate)).thenReturn(messageOut)

        mockMvc.post("/message") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(messageCreate)
        }.andExpect {
            status { isOk() }
            // duplicated assert -> similar to json path
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json(mapper.writeValueAsString(messageOut)) }
            jsonPath("$.id") { value("1") }
            jsonPath("$.text") { value("ING") }
        }

    }


}
