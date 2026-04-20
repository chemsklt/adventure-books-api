package com.adventure.book.exception.book;

public class InvalidBookException extends RuntimeException{

    public InvalidBookException(String message) {
        super(message);
    }
}
