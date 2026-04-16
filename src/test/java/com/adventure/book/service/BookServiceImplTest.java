package com.adventure.book.service;

import com.adventure.book.domain.*;
import com.adventure.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book prisonerBook;



    @BeforeEach
    void setUp() {
        Section begin = new Section(
                "1",
                "Start",
                SectionType.BEGIN,
                List.of(
                        new Option("Open the door", "500", null),
                        new Option("Look under the bed", "20", null)
                )
        );

        Section middle = new Section(
                "20",
                "Middle",
                SectionType.NODE,
                List.of(new Option("Continue", "1000", null))
        );

        Section lockedDoor = new Section(
                "500",
                "The door is locked",
                SectionType.NODE,
                List.of(new Option("Think", "1", null))
        );

        Section end = new Section(
                "1000",
                "Freedom",
                SectionType.END,
                null
        );

        prisonerBook = new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                new LinkedHashSet<>(),
                List.of(begin, middle, lockedDoor, end)
        );
    }

    @Test
    void shouldReturnBooksFilteredByDifficultyAndSortedByTitle() {
        Book easyBook = new Book(
                "crystal-caverns",
                "The Crystal Caverns",
                "Evelyn Stormrider",
                Difficulty.EASY,
                Set.of(),
                List.of()
        );

        Book hardBook = new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                Set.of(),
                List.of()
        );

        when(bookRepository.findAll()).thenReturn(List.of(hardBook, easyBook));

        List<Book> result = bookService.searchBooks(null, null, null, Difficulty.EASY);

        assertThat(result)
                .hasSize(1)
                .extracting(Book::getId)
                .containsExactly("crystal-caverns");
    }

    @Test
    void shouldReturnBookById() {
        when(bookRepository.findById("the-prisoner")).thenReturn(Optional.of(prisonerBook));

        Book result = bookService.getBookById("the-prisoner");

        assertThat(result.getId()).isEqualTo("the-prisoner");
    }
}
