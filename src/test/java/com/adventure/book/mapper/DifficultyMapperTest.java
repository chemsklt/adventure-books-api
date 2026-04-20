package com.adventure.book.mapper;

import com.adventure.book.domain.book.Difficulty;
import com.adventure.book.mapper.book.DifficultyMapper;
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
                .isEqualTo(Difficulty.EASY);

        assertThat(mapper.toDomain(com.adventure.book.generated.model.Difficulty.MEDIUM))
                .isEqualTo(Difficulty.MEDIUM);

        assertThat(mapper.toDomain(com.adventure.book.generated.model.Difficulty.HARD))
                .isEqualTo(Difficulty.HARD);
    }
}