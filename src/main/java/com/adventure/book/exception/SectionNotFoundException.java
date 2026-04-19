package com.adventure.book.exception;

public class SectionNotFoundException extends RuntimeException {

    public SectionNotFoundException(String bookId, String sectionId) {
        super("Section with id '%s' was not found in book '%s'".formatted(sectionId, bookId));
    }
}
