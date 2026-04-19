package com.adventure.book.mapper.game;

import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.ConsequenceType;
import com.adventure.book.domain.game.GameMoveResult;
import com.adventure.book.domain.game.GameStatus;
import com.adventure.book.generated.model.ConsequenceResponse;
import com.adventure.book.generated.model.GameResponse;
import com.adventure.book.mapper.BookReadingMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BookReadingMapper.class)
public interface GameMapper {

    @Mapping(target = "gameId", source = "gameSession.id")
    @Mapping(target = "bookId", source = "gameSession.bookId")
    @Mapping(target = "health", source = "gameSession.health")
    @Mapping(target = "status", source = "gameSession.status")
    @Mapping(target = "currentSection", source = "currentSection")
    @Mapping(target = "appliedConsequence", source = "appliedConsequence")
    GameResponse toGameResponse(GameMoveResult result);

    ConsequenceResponse toConsequenceResponse(Consequence consequence);

    com.adventure.book.generated.model.GameStatus map(GameStatus status);

    com.adventure.book.generated.model.ConsequenceType map(ConsequenceType type);
}