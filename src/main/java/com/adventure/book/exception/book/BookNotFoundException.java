package com.adventure.book.exception.book;

public class BookNotFoundException extends RuntimeException{
    public BookNotFoundException(String bookId) {
        super("Book with id '%s' was not found".formatted(bookId));
    }
}
