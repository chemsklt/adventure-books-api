package com.adventure.book.service;

import com.adventure.book.domain.Book;
import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.ConsequenceType;
import com.adventure.book.domain.Difficulty;
import com.adventure.book.domain.game.GameMoveResult;
import com.adventure.book.domain.game.GameSession;
import com.adventure.book.domain.game.GameStatus;
import com.adventure.book.domain.Option;
import com.adventure.book.domain.Section;
import com.adventure.book.domain.SectionType;
import com.adventure.book.exception.game.GameNotFoundException;
import com.adventure.book.exception.OptionNotFoundException;
import com.adventure.book.repository.game.GameSessionRepository;
import com.adventure.book.service.game.GameServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private BookService bookService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void shouldStartGameWithInitialHealthAndBeginSection() {
        Section begin = createPrisonerBook().getSections().get(0);
        when(bookService.getStartSection("the-prisoner")).thenReturn(begin);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMoveResult result = gameService.startGame("the-prisoner");

        assertThat(result.getGameSession().getBookId()).isEqualTo("the-prisoner");
        assertThat(result.getGameSession().getHealth()).isEqualTo(10);
        assertThat(result.getGameSession().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(result.getCurrentSection().getId()).isEqualTo("1");
        assertThat(result.getAppliedConsequence()).isNull();

        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void shouldReturnGameStateById() {
        GameSession session = new GameSession("game-1", "the-prisoner", "1", 10, GameStatus.IN_PROGRESS);
        Section currentSection = createPrisonerBook().getSections().get(0);

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "1")).thenReturn(currentSection);

        GameMoveResult result = gameService.getGame("game-1");

        assertThat(result.getGameSession().getId()).isEqualTo("game-1");
        assertThat(result.getCurrentSection().getId()).isEqualTo("1");
        assertThat(result.getAppliedConsequence()).isNull();
    }

    @Test
    void shouldThrowWhenGameNotFound() {
        when(gameSessionRepository.findById("missing-game")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGame("missing-game"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game with id 'missing-game' was not found");
    }

    @Test
    void shouldChooseOptionWithoutConsequenceAndKeepHealth() {
        GameSession session = new GameSession("game-1", "the-prisoner", "1", 10, GameStatus.IN_PROGRESS);
        Book book = createPrisonerBook();
        Section currentSection = book.getSections().get(0);
        Section nextSection = book.getSections().get(3); // 500

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "1")).thenReturn(currentSection);
        when(bookService.getSection("the-prisoner", "500")).thenReturn(nextSection);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMoveResult result = gameService.chooseOption("game-1", "0");

        assertThat(result.getGameSession().getHealth()).isEqualTo(10);
        assertThat(result.getGameSession().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(result.getCurrentSection().getId()).isEqualTo("500");
        assertThat(result.getAppliedConsequence()).isNull();
    }

    @Test
    void shouldApplyLoseHealthConsequence() {
        GameSession session = new GameSession("game-1", "the-prisoner", "20", 10, GameStatus.IN_PROGRESS);
        Book book = createPrisonerBook();
        Section currentSection = book.getSections().get(1); // 20
        Section nextSection = book.getSections().get(2);    // 30

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "20")).thenReturn(currentSection);
        when(bookService.getSection("the-prisoner", "30")).thenReturn(nextSection);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMoveResult result = gameService.chooseOption("game-1", "0");

        assertThat(result.getGameSession().getHealth()).isEqualTo(4);
        assertThat(result.getGameSession().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(result.getCurrentSection().getId()).isEqualTo("30");
        assertThat(result.getAppliedConsequence()).isNotNull();
        assertThat(result.getAppliedConsequence().getType()).isEqualTo(ConsequenceType.LOSE_HEALTH);
        assertThat(result.getAppliedConsequence().getValue()).isEqualTo(6);
    }

    @Test
    void shouldMarkGameAsFinishedWhenEndSectionIsReached() {
        GameSession session = new GameSession("game-1", "the-prisoner", "30", 4, GameStatus.IN_PROGRESS);
        Book book = createPrisonerBook();
        Section currentSection = book.getSections().get(2); // 30
        Section endSection = book.getSections().get(4);     // 1000

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "30")).thenReturn(currentSection);
        when(bookService.getSection("the-prisoner", "1000")).thenReturn(endSection);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMoveResult result = gameService.chooseOption("game-1", "0");

        assertThat(result.getGameSession().getHealth()).isEqualTo(4);
        assertThat(result.getGameSession().getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(result.getCurrentSection().getType()).isEqualTo(SectionType.END);
    }

    @Test
    void shouldMarkGameAsDeadWhenHealthDropsToZero() {
        GameSession session = new GameSession("game-1", "the-prisoner", "500", 3, GameStatus.IN_PROGRESS);
        Book book = createPrisonerBook();
        Section currentSection = book.getSections().get(3); // 500
        Section nextSection = book.getSections().get(0);    // 1

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "500")).thenReturn(currentSection);
        when(bookService.getSection("the-prisoner", "1")).thenReturn(nextSection);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GameMoveResult result = gameService.chooseOption("game-1", "0");

        assertThat(result.getGameSession().getHealth()).isEqualTo(0);
        assertThat(result.getGameSession().getStatus()).isEqualTo(GameStatus.DEAD);
        assertThat(result.getCurrentSection().getId()).isEqualTo("1");
    }

    @Test
    void shouldThrowWhenOptionIdIsInvalid() {
        GameSession session = new GameSession("game-1", "the-prisoner", "1", 10, GameStatus.IN_PROGRESS);
        Section currentSection = createPrisonerBook().getSections().get(0);

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "1")).thenReturn(currentSection);

        assertThatThrownBy(() -> gameService.chooseOption("game-1", "99"))
                .isInstanceOf(OptionNotFoundException.class)
                .hasMessage("Option with id '99' was not found in section '1' for book 'the-prisoner'");
    }

    @Test
    void shouldRejectChoiceWhenGameIsFinished() {
        GameSession session = new GameSession("game-1", "the-prisoner", "1000", 4, GameStatus.FINISHED);
        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.chooseOption("game-1", "0"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Game 'game-1' is not in progress and cannot accept more choices");
    }

    @Test
    void shouldPersistUpdatedSessionOnChoice() {
        GameSession session = new GameSession("game-1", "the-prisoner", "20", 10, GameStatus.IN_PROGRESS);
        Book book = createPrisonerBook();
        Section currentSection = book.getSections().get(1);
        Section nextSection = book.getSections().get(2);

        when(gameSessionRepository.findById("game-1")).thenReturn(Optional.of(session));
        when(bookService.getSection("the-prisoner", "20")).thenReturn(currentSection);
        when(bookService.getSection("the-prisoner", "30")).thenReturn(nextSection);
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        gameService.chooseOption("game-1", "0");

        ArgumentCaptor<GameSession> captor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionRepository).save(captor.capture());

        GameSession saved = captor.getValue();
        assertThat(saved.getCurrentSectionId()).isEqualTo("30");
        assertThat(saved.getHealth()).isEqualTo(4);
        assertThat(saved.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    private Book createPrisonerBook() {
        return new Book(
                "the-prisoner",
                "The Prisoner",
                "Daniel El Fuego",
                Difficulty.HARD,
                new LinkedHashSet<>(),
                java.util.List.of(
                        new Section("1", "Start", SectionType.BEGIN, java.util.List.of(
                                new Option("Try the door", "500", null),
                                new Option("Look under the bed", "20", null)
                        )),
                        new Section("20", "Too dark", SectionType.NODE, java.util.List.of(
                                new Option(
                                        "Scan area",
                                        "30",
                                        new Consequence(
                                                ConsequenceType.LOSE_HEALTH,
                                                6,
                                                "You cut yourself on a rusty nail."
                                        )
                                )
                        )),
                        new Section("30", "Found key", SectionType.NODE, java.util.List.of(
                                new Option("Open the door", "1000", null)
                        )),
                        new Section("500", "Door locked", SectionType.NODE, java.util.List.of(
                                new Option(
                                        "Gather your thoughts",
                                        "1",
                                        new Consequence(
                                                ConsequenceType.LOSE_HEALTH,
                                                3,
                                                "You're getting crazier."
                                        )
                                )
                        )),
                        new Section("1000", "Freedom", SectionType.END, null)
                )
        );
    }
}
