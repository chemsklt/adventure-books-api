package com.adventure.book.service;

import com.adventure.book.domain.Book;
import com.adventure.book.domain.Difficulty;
import com.adventure.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<Book> searchBooks(String title, String author, String category, Difficulty difficulty) {
        return bookRepository.findAll().stream()
                .filter(book -> matches(book.getTitle(), title))
                .filter(book -> matches(book.getAuthor(), author))
                .filter(book -> matchesCategory(book, category))
                .filter(book -> difficulty == null || book.getDifficulty() == difficulty)
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .toList();
    }

    @Override
    public Book getBookById(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException(bookId));
    }

    private boolean matches(String fieldValue, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return fieldValue != null &&
                fieldValue.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    private boolean matchesCategory(Book book, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        return book.getCategories() != null &&
                book.getCategories().stream()
                        .anyMatch(existing -> existing.equalsIgnoreCase(category));
    }
}
