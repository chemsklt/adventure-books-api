package com.adventure.book.repository;

import com.adventure.book.domain.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> books = new ConcurrentHashMap<>();

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public Optional<Book> findById(String id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public Book save(Book book) {
        books.put(book.getId(), book);
        return book;
    }

    public void saveAll(List<Book> initialBooks) {
        initialBooks.forEach(book -> books.put(book.getId(), book));
    }
}