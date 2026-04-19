package com.adventure.book.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DifficultyMapper {

    com.adventure.book.domain.Difficulty toDomain(
            com.adventure.book.generated.model.Difficulty difficulty
    );
}