package com.adventure.book.domain.book;

import com.adventure.book.domain.Consequence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option {
    private String description;
    private String gotoId;
    private Consequence consequence;
}
