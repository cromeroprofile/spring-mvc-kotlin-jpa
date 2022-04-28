package com.example.kotlinmvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post


@WebMvcTest
class MessageControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockBean
    lateinit var messageService: MessageService

    @Autowired
    lateinit var mapper : ObjectMapper

    @Test
    fun `should create message with uppercase text`() {

        val message = Message(text = "ing")

        `when`(messageService.save(message)).thenReturn(Message(1, "ING"))

        mockMvc.perform(
            post("/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(message))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.text").value("ING"))

    }


    @Test
    fun `should findAll `() {

        `when`(messageService.findAll(null)).thenReturn(listOf(Message(1,"ING")))

        mockMvc.perform(get("/message"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].text").value("ING"))

    }


}
