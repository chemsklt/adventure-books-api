package com.adventure.book.controller.book;

import com.adventure.book.domain.book.Book;
import com.adventure.book.generated.api.BooksApi;
import com.adventure.book.generated.model.*;
import com.adventure.book.mapper.book.BookCreationMapper;
import com.adventure.book.mapper.book.BookMapper;
import com.adventure.book.mapper.book.DifficultyMapper;
import com.adventure.book.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController implements BooksApi {

    private final BookService bookService;
    private final BookMapper bookMapper;
    private final DifficultyMapper difficultyMapper;
    private final BookCreationMapper bookCreationMapper;

    @Override
    public ResponseEntity<BookDetailsResponse> addCategoryToBook(String bookId, CategoryRequest categoryRequest) {
        Book updatedBook = bookService.addCategory(bookId, categoryRequest.getName());
        return ResponseEntity.ok(bookMapper.toBookDetailsResponse(updatedBook));
    }

    @Override
    public ResponseEntity<BookDetailsResponse> createBook(CreateBookRequest createBookRequest) {
        Book book = bookCreationMapper.toBook(createBookRequest);
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookMapper.toBookDetailsResponse(createdBook));
    }

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

    @Override
    public ResponseEntity<Void> removeCategoryFromBook(String bookId, String categoryName) {
        bookService.removeCategory(bookId, categoryName);
        return ResponseEntity.noContent().build();
    }
}
