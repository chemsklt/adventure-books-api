package com.adventure.book.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListLoadedBooks() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].id", hasItem("crystal-caverns")))
                .andExpect(jsonPath("$.content[*].id", hasItem("pirates-jade-sea")))
                .andExpect(jsonPath("$.content[*].id", hasItem("the-prisoner")));
    }

    @Test
    void shouldGetBookDetailsById() throws Exception {
        mockMvc.perform(get("/books/the-prisoner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("the-prisoner"))
                .andExpect(jsonPath("$.title").value("The Prisoner"))
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.sectionsCount").value(6));
    }

    @Test
    void shouldReturn404ForUnknownBook() throws Exception {
        mockMvc.perform(get("/books/unknown-book"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book with id 'unknown-book' was not found"));
    }
}
