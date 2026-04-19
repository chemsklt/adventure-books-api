package com.adventure.book.controller.game;

import com.adventure.book.domain.game.GameMoveResult;
import com.adventure.book.generated.api.GamesApi;
import com.adventure.book.generated.model.GameChoiceRequest;
import com.adventure.book.generated.model.GameResponse;
import com.adventure.book.generated.model.StartGameRequest;
import com.adventure.book.mapper.game.GameMapper;
import com.adventure.book.service.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameController implements GamesApi {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @Override
    public ResponseEntity<GameResponse> startGame(StartGameRequest startGameRequest) {
        GameMoveResult result = gameService.startGame(startGameRequest.getBookId());
        return ResponseEntity.status(201).body(gameMapper.toGameResponse(result));
    }

    @Override
    public ResponseEntity<GameResponse> getGame(String gameId) {
        GameMoveResult result = gameService.getGame(gameId);
        return ResponseEntity.ok(gameMapper.toGameResponse(result));
    }

    @Override
    public ResponseEntity<GameResponse> chooseGameOption(String gameId, GameChoiceRequest gameChoiceRequest) {
        GameMoveResult result = gameService.chooseOption(gameId, gameChoiceRequest.getOptionId());
        return ResponseEntity.ok(gameMapper.toGameResponse(result));
    }
}
