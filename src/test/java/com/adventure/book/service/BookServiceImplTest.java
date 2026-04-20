package com.adventure.book.service;

import com.adventure.book.domain.book.*;
import com.adventure.book.exception.book.BookAlreadyExistsException;
import com.adventure.book.exception.book.BookNotFoundException;
import com.adventure.book.exception.book.InvalidBookException;
import com.adventure.book.repository.book.BookRepository;
import com.adventure.book.service.book.BookServiceImpl;
import com.adventure.book.service.book.BookValidationService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookValidationService bookValidationService;

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

    @Test
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById("missing"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book with id 'missing' was not found");
    }

    @Test
    void shouldAddCategoryWhenNotAlreadyPresent() {
        when(bookRepository.findById("the-prisoner")).thenReturn(Optional.of(prisonerBook));
        when(bookRepository.save(prisonerBook)).thenReturn(prisonerBook);

        Book result = bookService.addCategory("the-prisoner", "Escape");

        assertThat(result.getCategories()).containsExactly("Escape");
        verify(bookRepository).save(prisonerBook);
    }

    @Test
    void shouldNotDuplicateCategoryIgnoringCase() {
        prisonerBook.setCategories(new LinkedHashSet<>(Set.of("Escape")));

        when(bookRepository.findById("the-prisoner")).thenReturn(Optional.of(prisonerBook));
        when(bookRepository.save(prisonerBook)).thenReturn(prisonerBook);

        Book result = bookService.addCategory("the-prisoner", "escape");

        assertThat(result.getCategories()).containsExactly("Escape");
    }

    @Test
    void shouldRemoveCategoryIgnoringCase() {
        prisonerBook.setCategories(new LinkedHashSet<>(Set.of("Escape", "Prison")));

        when(bookRepository.findById("the-prisoner")).thenReturn(Optional.of(prisonerBook));
        when(bookRepository.save(prisonerBook)).thenReturn(prisonerBook);

        bookService.removeCategory("the-prisoner", "escape");

        assertThat(prisonerBook.getCategories()).containsExactly("Prison");
        verify(bookRepository).save(prisonerBook);
    }

    @Test
    void shouldCreateBook() {
        Book book = new Book(
                "new-book",
                "New Book",
                "Chems Keltoum",
                Difficulty.EASY,
                new java.util.LinkedHashSet<>(java.util.List.of(" Fantasy ", "Fantasy", "Mystery")),
                java.util.List.of(
                        new Section("1", "Start", SectionType.BEGIN, java.util.List.of(
                                new Option("Go", "2", null)
                        )),
                        new Section("2", "End", SectionType.END, null)
                )
        );

        when(bookRepository.existsById("new-book")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book created = bookService.createBook(book);

        assertThat(created.getId()).isEqualTo("new-book");
        assertThat(created.getCategories()).containsExactly("Fantasy", "Mystery");
        verify(bookValidationService).validate(book);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldThrowWhenCreatingDuplicateBook() {
        Book book = new Book(
                "existing-book",
                "Existing Book",
                "Chems Keltoum",
                Difficulty.EASY,
                new java.util.LinkedHashSet<>(),
                java.util.List.of()
        );

        when(bookRepository.existsById("existing-book")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(book))
                .isInstanceOf(BookAlreadyExistsException.class)
                .hasMessage("Book with id existing-book already exist");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldValidateBookBeforeSaving() {
        Book book = new Book(
                "broken-book",
                "Broken Book",
                "Chems Keltoum",
                Difficulty.EASY,
                new java.util.LinkedHashSet<>(),
                java.util.List.of()
        );

        when(bookRepository.existsById("broken-book")).thenReturn(false);
        doThrow(new InvalidBookException("Book 'broken-book' has no sections"))
                .when(bookValidationService).validate(book);

        assertThatThrownBy(() -> bookService.createBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("Book 'broken-book' has no sections");

        verify(bookRepository, never()).save(any());
    }
}
