package com.adventure.book.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consequence {
    private ConsequenceType type;
    private Integer value;
    private String text;
}
