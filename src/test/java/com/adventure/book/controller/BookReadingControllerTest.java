package com.adventure.book.controller;

import com.adventure.book.controller.book.BookReadingController;
import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.exception.book.BookNotFoundException;
import com.adventure.book.exception.GlobalExceptionHandler;
import com.adventure.book.exception.book.OptionNotFoundException;
import com.adventure.book.exception.book.SectionNotFoundException;
import com.adventure.book.generated.model.OptionResponse;
import com.adventure.book.generated.model.SectionResponse;
import com.adventure.book.mapper.book.BookReadingMapper;
import com.adventure.book.service.book.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookReadingController.class)
@Import(GlobalExceptionHandler.class)
class BookReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookReadingMapper bookReadingMapper;

    @Test
    void shouldStartBook() throws Exception {
        Section section = new Section(
                "1",
                "Start text",
                SectionType.BEGIN,
                List.of(
                        new Option("Open the door", "500", null),
                        new Option("Look under the bed", "20", null)
                )
        );

        SectionResponse response = new SectionResponse()
                .sectionId("1")
                .text("Start text")
                .type(SectionResponse.TypeEnum.BEGIN)
                .options(List.of(
                        new OptionResponse().id("0").text("Open the door").nextSectionId("500"),
                        new OptionResponse().id("1").text("Look under the bed").nextSectionId("20")
                ));

        when(bookService.getStartSection("the-prisoner")).thenReturn(section);
        when(bookReadingMapper.toSectionResponse(section)).thenReturn(response);

        mockMvc.perform(get("/books/the-prisoner/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("1"))
                .andExpect(jsonPath("$.type").value("BEGIN"))
                .andExpect(jsonPath("$.options[0].id").value("0"));
    }

    @Test
    void shouldGetSectionById() throws Exception {
        Section section = new Section(
                "500",
                "The door is locked.",
                SectionType.NODE,
                List.of(new Option("Gather your thoughts", "1", null))
        );

        SectionResponse response = new SectionResponse()
                .sectionId("500")
                .text("The door is locked.")
                .type(SectionResponse.TypeEnum.NODE)
                .options(List.of(
                        new OptionResponse().id("0").text("Gather your thoughts").nextSectionId("1")
                ));

        when(bookService.getSection("the-prisoner", "500")).thenReturn(section);
        when(bookReadingMapper.toSectionResponse(section)).thenReturn(response);

        mockMvc.perform(get("/books/the-prisoner/sections/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionId").value("500"))
                .andExpect(jsonPath("$.type").value("NODE"));
    }

    @Test
    void shouldChooseOptionAndReturnNextSection() throws Exception {
        Section nextSection = new Section(
                "500",
                "The door is locked.",
                SectionType.NODE,
                List.of(new Option("Gather your thoughts", "1", null))
        );

        SectionResponse response = new SectionResponse()
                .sectionId("500")
                .text("The door is locked.")
                .type(SectionResponse.TypeEnum.NODE)
                .options(List.of(
                        new OptionResponse().id("0").text("Gather your thoughts").nextSectionId("1")
                ));

        when(bookService.chooseOption("the-prisoner", "1", "0")).thenReturn(nextSection);
        when(bookReadingMapper.toSectionResponse(nextSection)).thenReturn(response);

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

        verify(bookService).chooseOption("the-prisoner", "1", "0");
    }

    @Test
    void shouldReturn404WhenStartingUnknownBook() throws Exception {
        when(bookService.getStartSection("unknown-book"))
                .thenThrow(new BookNotFoundException("unknown-book"));

        mockMvc.perform(get("/books/unknown-book/start"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book with id 'unknown-book' was not found"));
    }

    @Test
    void shouldReturn404WhenSectionNotFound() throws Exception {
        when(bookService.getSection("the-prisoner", "999"))
                .thenThrow(new SectionNotFoundException("the-prisoner", "999"));

        mockMvc.perform(get("/books/the-prisoner/sections/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Section with id '999' was not found in book 'the-prisoner'"));
    }

    @Test
    void shouldReturn404WhenOptionNotFound() throws Exception {
        when(bookService.chooseOption("the-prisoner", "1", "999"))
                .thenThrow(new OptionNotFoundException("the-prisoner", "1", "999"));

        mockMvc.perform(post("/books/the-prisoner/sections/1/choose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "999"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Option with id '999' was not found in section '1' for book 'the-prisoner'"));
    }

    @Test
    void shouldReturn405WhenWrongMethodUsedOnChooseEndpoint() throws Exception {
        mockMvc.perform(get("/books/the-prisoner/sections/1/choose"))
                .andExpect(status().isMethodNotAllowed());
    }
}