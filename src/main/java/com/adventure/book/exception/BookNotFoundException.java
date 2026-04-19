package com.adventure.book.exception;

public class BookNotFoundException extends RuntimeException{
    public BookNotFoundException(String bookId) {
        super("Book with id '%s' was not found".formatted(bookId));
    }
}
