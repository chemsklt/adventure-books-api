package com.adventure.book.mapper;

import org.springframework.stereotype.Component;

@Component
public class DifficultyMapper {

    public com.adventure.book.domain.Difficulty toDomain(
            com.adventure.book.generated.model.Difficulty difficulty
    ) {
        if (difficulty == null) {
            return null;
        }
        return com.adventure.book.domain.Difficulty.valueOf(difficulty.name());
    }
}