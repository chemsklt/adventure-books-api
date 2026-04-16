package com.adventure.book.config;

import com.adventure.book.domain.Book;
import com.adventure.book.repository.InMemoryBookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookDataLoader implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final InMemoryBookRepository bookRepository;

    @Override
    public void run(String... args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:books/*.json");

        List<Book> books = new ArrayList<>();

        for (Resource resource : resources) {
            String filename = resource.getFilename();

            if (resource.contentLength() == 0) {
                log.warn("Skipping empty book file: {}", filename);
                continue;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Book book = objectMapper.readValue(inputStream, Book.class);

                String bookId = filename != null
                        ? filename.replace(".json", "")
                        : UUID.randomUUID().toString();

                book.setId(bookId);

                if (book.getCategories() == null) {
                    book.setCategories(new LinkedHashSet<>());
                }
                books.add(book);
                log.info("Loaded book: {}", bookId);
            } catch (Exception ex) {
                log.warn("Skipping invalid book file: {} - {}", filename, ex.getMessage());
            }
        }

        bookRepository.saveAll(books);
        log.info("Loaded {} books into memory", books.size());
    }
}