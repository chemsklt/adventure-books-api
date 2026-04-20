package com.adventure.book.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void shouldAddAndRemoveCategory() throws Exception {
        mockMvc.perform(post("/books/the-prisoner/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Escape"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0]").value("Escape"));

        mockMvc.perform(delete("/books/the-prisoner/categories/Escape"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/books/the-prisoner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(0));
    }

    @Test
    void shouldCreateAndRetrieveBook() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "new-book",
                                  "title": "New Book",
                                  "author": "Chems Keltoum",
                                  "difficulty": "EASY",
                                  "categories": ["Fantasy", "Mystery"],
                                  "sections": [
                                    {
                                      "id": "1",
                                      "text": "Start",
                                      "type": "BEGIN",
                                      "options": [
                                        {
                                          "description": "Go to the end",
                                          "gotoId": "2"
                                        }
                                      ]
                                    },
                                    {
                                      "id": "2",
                                      "text": "The End",
                                      "type": "END"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new-book"))
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.sectionsCount").value(2));

        mockMvc.perform(get("/books/new-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-book"))
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.author").value("Chems Keltoum"))
                .andExpect(jsonPath("$.difficulty").value("EASY"));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='new-book')].title").value(org.hamcrest.Matchers.hasItem("New Book")));
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateBook() throws Exception {
        String payload = """
                {
                  "id": "duplicate-book",
                  "title": "Duplicate Book",
                  "author": "Chems Keltoum",
                  "difficulty": "EASY",
                  "categories": [],
                  "sections": [
                    {
                      "id": "1",
                      "text": "Start",
                      "type": "BEGIN",
                      "options": [
                        {
                          "description": "Finish",
                          "gotoId": "2"
                        }
                      ]
                    },
                    {
                      "id": "2",
                      "text": "End",
                      "type": "END"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book with id duplicate-book already exist"));
    }
}
