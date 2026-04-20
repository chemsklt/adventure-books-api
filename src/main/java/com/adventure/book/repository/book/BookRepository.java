package com.adventure.book.repository.book;

import com.adventure.book.domain.book.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findById(String id);
    Book save(Book book);
}
