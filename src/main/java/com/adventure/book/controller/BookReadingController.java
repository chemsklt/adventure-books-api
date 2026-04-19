package com.adventure.book.controller;

import com.adventure.book.domain.Section;
import com.adventure.book.generated.api.BookReadingApi;
import com.adventure.book.generated.model.ChooseOptionRequest;
import com.adventure.book.generated.model.SectionResponse;
import com.adventure.book.mapper.BookReadingMapper;
import com.adventure.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookReadingController implements BookReadingApi {

    private final BookService bookService;
    private final BookReadingMapper bookReadingMapper;


    @Override
    public ResponseEntity<SectionResponse> startBook(String bookId) {
        Section startSection = bookService.getStartSection(bookId);
        return ResponseEntity.ok(bookReadingMapper.toSectionResponse(startSection));
    }

    @Override
    public ResponseEntity<SectionResponse> getSection(String bookId, String sectionId) {
        Section section = bookService.getSection(bookId, sectionId);
        return ResponseEntity.ok(bookReadingMapper.toSectionResponse(section));
    }

    @Override
    public ResponseEntity<SectionResponse> chooseOption(
            String bookId,
            String sectionId,
            ChooseOptionRequest request
    ) {
        Section nextSection = bookService.chooseOption(bookId, sectionId, request.getOptionId());
        return ResponseEntity.ok(bookReadingMapper.toSectionResponse(nextSection));
    }
}