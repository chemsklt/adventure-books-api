package com.adventure.book.repository.game;

import com.adventure.book.domain.game.GameSession;

import java.util.Optional;

public interface GameSessionRepository {
    GameSession save(GameSession session);
    Optional<GameSession> findById(String id);
}
