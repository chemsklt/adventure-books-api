package com.adventure.book.controller;

import com.adventure.book.domain.Book;
import com.adventure.book.generated.api.BooksApi;
import com.adventure.book.generated.model.BookDetailsResponse;
import com.adventure.book.generated.model.BookSummaryListResponse;
import com.adventure.book.generated.model.BookSummaryResponse;
import com.adventure.book.generated.model.Difficulty;
import com.adventure.book.mapper.BookMapper;
import com.adventure.book.mapper.DifficultyMapper;
import com.adventure.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController implements BooksApi {

    private final BookService bookService;
    private final BookMapper bookMapper;
    private final DifficultyMapper difficultyMapper;

    @Override
    public ResponseEntity<BookDetailsResponse> getBookById(String bookId) {
        Book book = bookService.getBookById(bookId);
        return ResponseEntity.ok(bookMapper.toBookDetailsResponse(book));
    }

    @Override
    public ResponseEntity<BookSummaryListResponse> listBooks(String title, String author, String category, Difficulty difficulty) {
        List<Book> books = bookService.searchBooks(title, author, category, difficultyMapper.toDomain(difficulty));
        List<BookSummaryResponse> content = bookMapper.toBookSummaryResponseList(books);

        BookSummaryListResponse response = new BookSummaryListResponse()
                .content(content)
                .totalElements((long) content.size());

        return ResponseEntity.ok(response);
    }
}
