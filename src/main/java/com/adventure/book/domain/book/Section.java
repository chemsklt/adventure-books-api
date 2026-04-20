package com.adventure.book.domain.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    private String id;
    private String text;
    private SectionType type;
    private List<Option> options;
}
