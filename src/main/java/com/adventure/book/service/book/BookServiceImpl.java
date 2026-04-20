package com.adventure.book.service.book;

import com.adventure.book.domain.book.*;
import com.adventure.book.exception.book.BookAlreadyExistsException;
import com.adventure.book.exception.book.BookNotFoundException;
import com.adventure.book.exception.book.OptionNotFoundException;
import com.adventure.book.exception.book.SectionNotFoundException;
import com.adventure.book.repository.book.BookRepository;
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
    private final BookValidationService bookValidationService;


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

    @Override
    public Section getStartSection(String bookId) {
        Book book = getBookById(bookId);
        bookValidationService.validate(book);

        return book.getSections().stream()
                .filter(section -> section.getType() == SectionType.BEGIN)
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException(bookId, "BEGIN"));
    }

    @Override
    public Section getSection(String bookId, String sectionId) {
        Book book = getBookById(bookId);
        bookValidationService.validate(book);
        return findSection(book, sectionId);
    }

    private Section findSection(Book book, String sectionId){
        return book.getSections().stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new SectionNotFoundException(book.getId(), sectionId));
    }

    @Override
    public Section chooseOption(String bookId, String sectionId, String optionId) {
        Book book = getBookById(bookId);
        bookValidationService.validate(book);

        Section currentSection = findSection(book, sectionId);

        if (currentSection.getOptions() == null || currentSection.getOptions().isEmpty()) {
            throw new OptionNotFoundException(bookId, sectionId, optionId);
        }

        Option selectedOption = findOption(currentSection, optionId, bookId);
        return findSection(book, selectedOption.getGotoId());
    }

    private Option findOption(Section section, String optionId, String bookId) {
        int index;
        try {
            index = Integer.parseInt(optionId);
        } catch (NumberFormatException ex) {
            throw new OptionNotFoundException(bookId, section.getId(), optionId);
        }

        if (index < 0 || index >= section.getOptions().size()) {
            throw new OptionNotFoundException(bookId, section.getId(), optionId);
        }

        return section.getOptions().get(index);
    }

    @Override
    public Book createBook(Book book) {
        if (bookRepository.existsById(book.getId())) {
            throw new BookAlreadyExistsException(book.getId());
        }

        Set<String> normalizedCategories = book.getCategories() == null
                ? new LinkedHashSet<>()
                : book.getCategories().stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        book.setCategories(normalizedCategories);

        bookValidationService.validate(book);

        return bookRepository.save(book);
    }
}
