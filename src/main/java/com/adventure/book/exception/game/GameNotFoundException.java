package com.adventure.book.exception.game;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String gameId) {
        super("Game with id '%s' was not found".formatted(gameId));
    }
}
