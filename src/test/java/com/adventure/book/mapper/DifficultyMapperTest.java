package com.adventure.book.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class DifficultyMapperTest {

    private final DifficultyMapper mapper = Mappers.getMapper(DifficultyMapper.class);

    @Test
    void shouldReturnNullWhenDifficultyIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void shouldMapGeneratedDifficultyToDomainDifficulty() {
        assertThat(mapper.toDomain(com.adventure.book.generated.model.Difficulty.EASY))
                .isEqualTo(com.adventure.book.domain.Difficulty.EASY);

        assertThat(mapper.toDomain(com.adventure.book.generated.model.Difficulty.MEDIUM))
                .isEqualTo(com.adventure.book.domain.Difficulty.MEDIUM);

        assertThat(mapper.toDomain(com.adventure.book.generated.model.Difficulty.HARD))
                .isEqualTo(com.adventure.book.domain.Difficulty.HARD);
    }
}