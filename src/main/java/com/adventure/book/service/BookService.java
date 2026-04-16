package com.adventure.book.service;

import com.adventure.book.domain.Book;
import com.adventure.book.domain.Difficulty;

import java.util.List;

public interface BookService {

    List<Book> searchBooks(String title, String author, String category, Difficulty difficulty);

    Book getBookById(String bookId);
}
