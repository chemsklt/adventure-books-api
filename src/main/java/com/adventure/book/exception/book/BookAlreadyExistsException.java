package com.adventure.book.exception.book;

public class BookAlreadyExistsException extends RuntimeException{

    public BookAlreadyExistsException(String bookId){
        super("Book with id %s already exist".formatted(bookId));
    }
}
