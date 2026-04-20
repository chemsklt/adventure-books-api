package com.adventure.book.mapper.book;

import com.adventure.book.domain.book.Book;
import com.adventure.book.generated.model.BookDetailsResponse;
import com.adventure.book.generated.model.BookSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookSummaryResponse toBookSummaryResponse(Book book);
    List<BookSummaryResponse> toBookSummaryResponseList(List<Book> books);

    @Mapping(target = "sectionsCount", expression = "java(book.getSections() == null ? 0 : book.getSections().size())")
    BookDetailsResponse toBookDetailsResponse(Book book);
}
