package com.adventure.book.service;

import com.adventure.book.domain.Book;
import com.adventure.book.domain.Difficulty;
import com.adventure.book.exception.BookNotFoundException;
import com.adventure.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
                .orElseThrow(() -> new BookNotFoundException(bookId));
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

    @Override
    public Book addCategory(String bookId, String category) {
        Book book = getBookById(bookId);
        String normalizedCategory = category == null ? null : category.trim();

        Set<String> categories = book.getCategories() == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(book.getCategories());

        if (normalizedCategory != null && !normalizedCategory.isBlank()) {
            boolean alreadyExists = categories.stream()
                    .anyMatch(existing -> existing.equalsIgnoreCase(normalizedCategory));

            if (!alreadyExists) {
                categories.add(normalizedCategory);
            }
        }

        book.setCategories(categories);
        return bookRepository.save(book);
    }

    @Override
    public void removeCategory(String bookId, String categoryName) {
        Book book = getBookById(bookId);

        if (book.getCategories() != null) {
            book.getCategories().removeIf(existing -> existing.equalsIgnoreCase(categoryName));
        }
        bookRepository.save(book);
    }
}
