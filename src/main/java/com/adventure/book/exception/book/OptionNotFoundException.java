package com.adventure.book.exception.book;

public class OptionNotFoundException extends RuntimeException {

    public OptionNotFoundException(String bookId, String sectionId, String optionId) {
        super("Option with id '%s' was not found in section '%s' for book '%s'"
                .formatted(optionId, sectionId, bookId));
    }
}
