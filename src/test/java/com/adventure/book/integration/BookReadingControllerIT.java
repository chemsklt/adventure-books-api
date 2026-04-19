package com.adventure.book.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookReadingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldStartBookAndReturnBeginSection() throws Exception {
        mockMvc.perform(get("/books/the-prisoner/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("1"))
                .andExpect(jsonPath("$.type").value("BEGIN"))
                .andExpect(jsonPath("$.options[0].id").value("0"))
                .andExpect(jsonPath("$.options[0].nextSectionId").value("500"));
    }

    @Test
    void shouldGetSectionById() throws Exception {
        mockMvc.perform(get("/books/the-prisoner/sections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("1"))
                .andExpect(jsonPath("$.type").value("BEGIN"));
    }

    @Test
    void shouldChooseOptionAndReturnNextSection() throws Exception {
        mockMvc.perform(post("/books/the-prisoner/sections/1/choose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("500"))
                .andExpect(jsonPath("$.type").value("NODE"));
    }

    @Test
    void shouldReturn404WhenSectionDoesNotExist() throws Exception {
        mockMvc.perform(get("/books/the-prisoner/sections/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Section with id '999' was not found in book 'the-prisoner'"));
    }

    @Test
    void shouldReturn404WhenOptionDoesNotExist() throws Exception {
        mockMvc.perform(post("/books/the-prisoner/sections/1/choose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "99"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Option with id '99' was not found in section '1' for book 'the-prisoner'"));
    }
}