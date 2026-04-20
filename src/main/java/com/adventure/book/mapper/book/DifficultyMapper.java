package com.adventure.book.mapper.book;

import com.adventure.book.domain.book.Difficulty;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DifficultyMapper {

    Difficulty toDomain(
            com.adventure.book.generated.model.Difficulty difficulty
    );
}