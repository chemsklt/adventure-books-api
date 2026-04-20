package com.adventure.book.controller;

import com.adventure.book.controller.book.BookController;
import com.adventure.book.domain.book.Book;
import com.adventure.book.domain.book.Difficulty;
import com.adventure.book.exception.book.BookAlreadyExistsException;
import com.adventure.book.exception.book.BookNotFoundException;
import com.adventure.book.exception.book.InvalidBookException;
import com.adventure.book.generated.model.BookDetailsResponse;
import com.adventure.book.generated.model.BookSummaryResponse;
import com.adventure.book.generated.model.CreateBookRequest;
import com.adventure.book.mapper.book.BookCreationMapper;
import com.adventure.book.mapper.book.BookMapper;
import com.adventure.book.mapper.book.DifficultyMapper;
import com.adventure.book.service.book.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private BookCreationMapper bookCreationMapper;

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

    @Test
    void shouldAddCategoryToBook() throws Exception {
        Book updatedBook = new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                new LinkedHashSet<>(List.of("Escape")),
                List.of()
        );

        BookDetailsResponse detailsResponse = new BookDetailsResponse()
                .id("the-prisoner")
                .title("The Prisoner")
                .author("Daniel El Fuego")
                .difficulty(com.adventure.book.generated.model.Difficulty.HARD)
                .categories(List.of("Escape"))
                .sectionsCount(6);

        when(bookService.addCategory("the-prisoner", "Escape")).thenReturn(updatedBook);
        when(bookMapper.toBookDetailsResponse(updatedBook)).thenReturn(detailsResponse);

        mockMvc.perform(post("/books/the-prisoner/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Escape"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("the-prisoner"))
                .andExpect(jsonPath("$.categories[0]").value("Escape"));
    }

    @Test
    void shouldReturn400WhenCategoryRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/books/the-prisoner/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRemoveCategoryFromBook() throws Exception {
        mockMvc.perform(delete("/books/the-prisoner/categories/Escape"))
                .andExpect(status().isNoContent());

        verify(bookService).removeCategory("the-prisoner", "Escape");
    }

    @Test
    void shouldReturn404WhenRemovingCategoryFromUnknownBook() throws Exception {
        org.mockito.Mockito.doThrow(new BookNotFoundException("missing-book"))
                .when(bookService).removeCategory("missing-book", "Escape");

        mockMvc.perform(delete("/books/missing-book/categories/Escape"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book with id 'missing-book' was not found"));
    }

    @Test
    void shouldCreateBook() throws Exception {
        Book domainBook = new Book(
                "new-book",
                "New Book",
                "Chems Keltoum",
                com.adventure.book.domain.book.Difficulty.EASY,
                new java.util.LinkedHashSet<>(java.util.List.of("Fantasy")),
                java.util.List.of()
        );

        BookDetailsResponse response = new BookDetailsResponse()
                .id("new-book")
                .title("New Book")
                .author("Chems Keltoum")
                .difficulty(com.adventure.book.generated.model.Difficulty.EASY)
                .categories(java.util.List.of("Fantasy"))
                .sectionsCount(0);

        when(bookCreationMapper.toBook(any(CreateBookRequest.class))).thenReturn(domainBook);
        when(bookService.createBook(domainBook)).thenReturn(domainBook);
        when(bookMapper.toBookDetailsResponse(domainBook)).thenReturn(response);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": "new-book",
                              "title": "New Book",
                              "author": "Chems Keltoum",
                              "difficulty": "EASY",
                              "categories": ["Fantasy"],
                              "sections": []
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("new-book"))
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.author").value("Chems Keltoum"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.sectionsCount").value(0));
    }

    @Test
    void shouldReturnConflictWhenBookAlreadyExists() throws Exception {
        Book domainBook = new Book(
                "existing-book",
                "Existing Book",
                "Chems Keltoum",
                com.adventure.book.domain.book.Difficulty.EASY,
                new java.util.LinkedHashSet<>(),
                java.util.List.of()
        );

        when(bookCreationMapper.toBook(any(CreateBookRequest.class))).thenReturn(domainBook);
        when(bookService.createBook(domainBook)).thenThrow(new BookAlreadyExistsException("existing-book"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": "existing-book",
                              "title": "Existing Book",
                              "author": "Chems Keltoum",
                              "difficulty": "EASY",
                              "categories": [],
                              "sections": []
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book with id existing-book already exist"));
    }

    @Test
    void shouldReturnBadRequestWhenBookStructureIsInvalid() throws Exception {
        Book invalidBook = new Book(
                "broken-book",
                "Broken Book",
                "John Doe",
                com.adventure.book.domain.book.Difficulty.EASY,
                new java.util.LinkedHashSet<>(),
                java.util.List.of()
        );

        when(bookCreationMapper.toBook(any(CreateBookRequest.class))).thenReturn(invalidBook);
        when(bookService.createBook(invalidBook))
                .thenThrow(new InvalidBookException("Book 'broken-book' has no sections"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": "broken-book",
                              "title": "Broken Book",
                              "author": "Chems Keltoum",
                              "difficulty": "EASY",
                              "categories": [],
                              "sections": []
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book 'broken-book' has no sections"));
    }
}