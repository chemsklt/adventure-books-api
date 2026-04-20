package com.adventure.book.service.game;

import com.adventure.book.domain.game.GameMoveResult;

public interface GameService {
    GameMoveResult startGame(String bookId);
    GameMoveResult getGame(String gameId);
    GameMoveResult chooseOption(String gameId, String optionId);
    GameMoveResult pauseGame(String gameId);
    GameMoveResult resumeGame(String gameId);
}
