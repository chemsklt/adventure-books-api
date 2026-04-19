package com.adventure.book.exception;

public class InvalidBookException extends RuntimeException{

    public InvalidBookException(String message) {
        super(message);
    }
}
