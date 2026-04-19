package com.adventure.book.controller;

import com.adventure.book.domain.Book;
import com.adventure.book.domain.Difficulty;
import com.adventure.book.exception.BookNotFoundException;
import com.adventure.book.generated.model.BookDetailsResponse;
import com.adventure.book.generated.model.BookSummaryResponse;
import com.adventure.book.mapper.BookMapper;
import com.adventure.book.mapper.DifficultyMapper;
import com.adventure.book.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean
    private DifficultyMapper difficultyMapper;

    @Test
    void shouldListBooks() throws Exception {
        Book domainBook = new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                new LinkedHashSet<>(),
                List.of()
        );

        BookSummaryResponse summaryResponse = new BookSummaryResponse()
                .id("the-prisoner")
                .title("The Prisoner")
                .author("Daniel El Fuego")
                .difficulty(com.adventure.book.generated.model.Difficulty.HARD)
                .categories(List.of());

        when(difficultyMapper.toDomain(isNull())).thenReturn(null);
        when(bookService.searchBooks(null, null, null, null)).thenReturn(List.of(domainBook));
        when(bookMapper.toBookSummaryResponseList(List.of(domainBook))).thenReturn(List.of(summaryResponse));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("the-prisoner"))
                .andExpect(jsonPath("$.content[0].title").value("The Prisoner"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldListBooksFilteredByDifficulty() throws Exception {
        Book domainBook = new Book(
                "crystal-caverns",
                "The Crystal Caverns",
                "Evelyn Stormrider",
                Difficulty.EASY,
                new LinkedHashSet<>(),
                List.of()
        );

        BookSummaryResponse summaryResponse = new BookSummaryResponse()
                .id("crystal-caverns")
                .title("The Crystal Caverns")
                .author("Evelyn Stormrider")
                .difficulty(com.adventure.book.generated.model.Difficulty.EASY)
                .categories(List.of());

        when(difficultyMapper.toDomain(com.adventure.book.generated.model.Difficulty.EASY)).thenReturn(Difficulty.EASY);
        when(bookService.searchBooks(null, null, null, Difficulty.EASY)).thenReturn(List.of(domainBook));
        when(bookMapper.toBookSummaryResponseList(List.of(domainBook))).thenReturn(List.of(summaryResponse));

        mockMvc.perform(get("/books").param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("crystal-caverns"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldGetBookById() throws Exception {
        Book domainBook = new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                new LinkedHashSet<>(),
                List.of()
        );

        BookDetailsResponse detailsResponse = new BookDetailsResponse()
                .id("the-prisoner")
                .title("The Prisoner")
                .author("Daniel El Fuego")
                .difficulty(com.adventure.book.generated.model.Difficulty.HARD)
                .categories(List.of())
                .sectionsCount(6);

        when(bookService.getBookById("the-prisoner")).thenReturn(domainBook);
        when(bookMapper.toBookDetailsResponse(domainBook)).thenReturn(detailsResponse);

        mockMvc.perform(get("/books/the-prisoner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("the-prisoner"))
                .andExpect(jsonPath("$.title").value("The Prisoner"))
                .andExpect(jsonPath("$.sectionsCount").value(6));
    }

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        when(bookService.getBookById("missing-book"))
                .thenThrow(new BookNotFoundException("missing-book"));

        mockMvc.perform(get("/books/missing-book"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book with id 'missing-book' was not found"));
    }
}