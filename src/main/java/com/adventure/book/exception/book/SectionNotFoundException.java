package com.adventure.book.exception.book;

public class SectionNotFoundException extends RuntimeException {

    public SectionNotFoundException(String bookId, String sectionId) {
        super("Section with id '%s' was not found in book '%s'".formatted(sectionId, bookId));
    }
}
