package com.adventure.book.controller;

import com.adventure.book.controller.game.GameController;
import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.ConsequenceType;
import com.adventure.book.domain.game.GameMoveResult;
import com.adventure.book.domain.game.GameSession;
import com.adventure.book.domain.game.GameStatus;
import com.adventure.book.domain.Section;
import com.adventure.book.domain.SectionType;
import com.adventure.book.exception.game.GameNotFoundException;
import com.adventure.book.exception.GlobalExceptionHandler;
import com.adventure.book.exception.InvalidBookException;
import com.adventure.book.generated.model.ConsequenceResponse;
import com.adventure.book.generated.model.GameResponse;
import com.adventure.book.generated.model.OptionResponse;
import com.adventure.book.generated.model.SectionResponse;
import com.adventure.book.mapper.game.GameMapper;
import com.adventure.book.service.game.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GameController.class)
@Import(GlobalExceptionHandler.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private GameMapper gameMapper;

    @Test
    void shouldStartGame() throws Exception {
        GameMoveResult result = new GameMoveResult(
                new GameSession("game-1", "the-prisoner", "1", 10, GameStatus.IN_PROGRESS),
                null,
                new Section("1", "Start", SectionType.BEGIN, List.of())
        );

        GameResponse response = new GameResponse()
                .gameId("game-1")
                .bookId("the-prisoner")
                .health(10)
                .status(com.adventure.book.generated.model.GameStatus.IN_PROGRESS)
                .currentSection(new SectionResponse()
                        .sectionId("1")
                        .text("Start")
                        .type(SectionResponse.TypeEnum.BEGIN)
                        .options(List.of()));

        when(gameService.startGame("the-prisoner")).thenReturn(result);
        when(gameMapper.toGameResponse(result)).thenReturn(response);

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "the-prisoner"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldGetGame() throws Exception {
        GameMoveResult result = new GameMoveResult(
                new GameSession("game-1", "the-prisoner", "1", 10, GameStatus.IN_PROGRESS),
                null,
                new Section("1", "Start", SectionType.BEGIN, List.of())
        );

        GameResponse response = new GameResponse()
                .gameId("game-1")
                .bookId("the-prisoner")
                .health(10)
                .status(com.adventure.book.generated.model.GameStatus.IN_PROGRESS)
                .currentSection(new SectionResponse()
                        .sectionId("1")
                        .text("Start")
                        .type(SectionResponse.TypeEnum.BEGIN)
                        .options(List.of()));

        when(gameService.getGame("game-1")).thenReturn(result);
        when(gameMapper.toGameResponse(result)).thenReturn(response);

        mockMvc.perform(get("/games/game-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.bookId").value("the-prisoner"));
    }

    @Test
    void shouldChooseGameOption() throws Exception {
        GameMoveResult result = new GameMoveResult(
                new GameSession("game-1", "the-prisoner", "30", 4, GameStatus.IN_PROGRESS),
                new Consequence(ConsequenceType.LOSE_HEALTH, 6, "You cut yourself."),
                new Section("30", "Found key", SectionType.NODE, List.of())
        );

        GameResponse response = new GameResponse()
                .gameId("game-1")
                .bookId("the-prisoner")
                .health(4)
                .status(com.adventure.book.generated.model.GameStatus.IN_PROGRESS)
                .appliedConsequence(new ConsequenceResponse()
                        .type(com.adventure.book.generated.model.ConsequenceType.LOSE_HEALTH)
                        .value(6)
                        .text("You cut yourself."))
                .currentSection(new SectionResponse()
                        .sectionId("30")
                        .text("Found key")
                        .type(SectionResponse.TypeEnum.NODE)
                        .options(List.of(
                                new OptionResponse().id("0").text("Open").nextSectionId("1000")
                        )));

        when(gameService.chooseOption("game-1", "0")).thenReturn(result);
        when(gameMapper.toGameResponse(result)).thenReturn(response);

        mockMvc.perform(post("/games/game-1/choices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(4))
                .andExpect(jsonPath("$.appliedConsequence.type").value("LOSE_HEALTH"))
                .andExpect(jsonPath("$.currentSection.sectionId").value("30"));
    }

    @Test
    void shouldReturn404WhenGameNotFound() throws Exception {
        when(gameService.getGame("missing-game"))
                .thenThrow(new GameNotFoundException("missing-game"));

        mockMvc.perform(get("/games/missing-game"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game with id 'missing-game' was not found"));
    }

    @Test
    void shouldReturn400WhenBookIsInvalid() throws Exception {
        when(gameService.startGame("crystal-caverns"))
                .thenThrow(new InvalidBookException(
                        "Section '666' in book 'crystal-caverns' is not END and must contain at least one option"
                ));

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "crystal-caverns"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Section '666' in book 'crystal-caverns' is not END and must contain at least one option"
                ));
    }
}
