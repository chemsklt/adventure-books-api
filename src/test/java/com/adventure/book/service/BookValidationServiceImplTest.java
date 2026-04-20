package com.adventure.book.service;

import com.adventure.book.domain.book.Book;
import com.adventure.book.domain.book.Difficulty;
import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.exception.book.InvalidBookException;
import com.adventure.book.service.book.BookValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookValidationServiceImplTest {

    private BookValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new BookValidationService();
    }

    @Test
    void shouldValidateReachableBookSuccessfully() {
        Book book = new Book(
                "book-1",
                "Valid Book",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(
                        new Section("1", "Start", SectionType.BEGIN,
                                List.of(new Option("Go", "2", null))),
                        new Section("2", "End", SectionType.END, null),
                        new Section("666", "Unreachable orphan", SectionType.NODE, null)
                )
        );

        assertThatCode(() -> validationService.validate(book))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenNoSections() {
        Book book = new Book("book-1", "Empty", "Author", Difficulty.EASY, Set.of(), List.of());

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("Book 'book-1' has no sections");
    }

    @Test
    void shouldFailWhenNoBeginSection() {
        Book book = new Book(
                "book-1",
                "No Begin",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(new Section("2", "End", SectionType.END, null))
        );

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("must contain exactly one BEGIN section");
    }

    @Test
    void shouldFailWhenMultipleBeginSections() {
        Book book = new Book(
                "book-1",
                "Two Begin",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(
                        new Section("1", "Start", SectionType.BEGIN, List.of(new Option("Go", "3", null))),
                        new Section("2", "Start2", SectionType.BEGIN, List.of(new Option("Go", "3", null))),
                        new Section("3", "End", SectionType.END, null)
                )
        );

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("must contain exactly one BEGIN section");
    }

    @Test
    void shouldFailWhenReachableEndDoesNotExist() {
        Book book = new Book(
                "book-1",
                "No End",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(
                        new Section("1", "Start", SectionType.BEGIN,
                                List.of(new Option("Go", "2", null))),
                        new Section("2", "Middle", SectionType.NODE,
                                List.of(new Option("Back", "1", null)))
                )
        );

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("at least one reachable END section");
    }

    @Test
    void shouldFailWhenReachableNodeHasNoOptions() {
        Book book = new Book(
                "book-1",
                "Broken",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(
                        new Section("1", "Start", SectionType.BEGIN,
                                List.of(
                                        new Option("Go to broken node", "2", null),
                                        new Option("Go to end", "3", null)
                                )),
                        new Section("2", "Broken Node", SectionType.NODE, null),
                        new Section("3", "End", SectionType.END, null)
                )
        );

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("is not END and must contain at least one option");
    }

    @Test
    void shouldFailWhenGotoIdDoesNotExist() {
        Book book = new Book(
                "book-1",
                "Broken Link",
                "Author",
                Difficulty.EASY,
                Set.of(),
                List.of(
                        new Section("1", "Start", SectionType.BEGIN,
                                List.of(new Option("Go", "999", null))),
                        new Section("2", "End", SectionType.END, null)
                )
        );

        assertThatThrownBy(() -> validationService.validate(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("references unknown gotoId '999'");
    }
}