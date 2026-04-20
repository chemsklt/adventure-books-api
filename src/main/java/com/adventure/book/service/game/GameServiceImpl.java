package com.adventure.book.service.game;

import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.game.GameMoveResult;
import com.adventure.book.domain.game.GameSession;
import com.adventure.book.domain.game.GameStatus;
import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.exception.game.GameNotFoundException;
import com.adventure.book.exception.book.OptionNotFoundException;
import com.adventure.book.repository.game.GameSessionRepository;
import com.adventure.book.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private static final int INITIAL_HEALTH = 10;

    private final BookService bookService;
    private final GameSessionRepository gameSessionRepository;

    @Override
    public GameMoveResult startGame(String bookId) {
        Section startSection = bookService.getStartSection(bookId);

        GameSession session = new GameSession(
                UUID.randomUUID().toString(),
                bookId,
                startSection.getId(),
                INITIAL_HEALTH,
                GameStatus.IN_PROGRESS
        );

        gameSessionRepository.save(session);

        return new GameMoveResult(session, null, startSection);
    }

    @Override
    public GameMoveResult getGame(String gameId) {
        GameSession session = getSession(gameId);
        Section currentSection = bookService.getSection(session.getBookId(), session.getCurrentSectionId());
        return new GameMoveResult(session, null, currentSection);
    }

    @Override
    public GameMoveResult chooseOption(String gameId, String optionId) {
        GameSession session = getSession(gameId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Game '%s' is not in progress and cannot accept more choices".formatted(gameId)
            );
        }

        Section currentSection = bookService.getSection(session.getBookId(), session.getCurrentSectionId());

        if (currentSection.getOptions() == null || currentSection.getOptions().isEmpty()) {
            throw new OptionNotFoundException(session.getBookId(), currentSection.getId(), optionId);
        }

        Option selectedOption = findOption(currentSection, optionId, session.getBookId());
        Consequence consequence = selectedOption.getConsequence();

        int updatedHealth = applyConsequence(session.getHealth(), consequence);
        Section nextSection = bookService.getSection(session.getBookId(), selectedOption.getGotoId());

        session.setHealth(Math.max(updatedHealth, 0));
        session.setCurrentSectionId(nextSection.getId());
        session.setStatus(determineStatus(session.getHealth(), nextSection));

        gameSessionRepository.save(session);

        return new GameMoveResult(session, consequence, nextSection);
    }

    private GameSession getSession(String gameId) {
        return gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private Option findOption(Section section, String optionId, String bookId) {
        int index;
        try {
            index = Integer.parseInt(optionId);
        } catch (NumberFormatException ex) {
            throw new OptionNotFoundException(bookId, section.getId(), optionId);
        }

        if (index < 0 || index >= section.getOptions().size()) {
            throw new OptionNotFoundException(bookId, section.getId(), optionId);
        }

        return section.getOptions().get(index);
    }

    private int applyConsequence(int currentHealth, Consequence consequence) {
        if (consequence == null || consequence.getType() == null || consequence.getValue() == null) {
            return currentHealth;
        }

        return switch (consequence.getType()) {
            case LOSE_HEALTH -> currentHealth - consequence.getValue();
            case GAIN_HEALTH -> currentHealth + consequence.getValue();
        };
    }

    private GameStatus determineStatus(int health, Section section) {
        if (health <= 0) {
            return GameStatus.DEAD;
        }
        if (section.getType() == SectionType.END) {
            return GameStatus.FINISHED;
        }
        return GameStatus.IN_PROGRESS;
    }

    @Override
    public GameMoveResult pauseGame(String gameId) {
        GameSession session = getSession(gameId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Game '%s' cannot be paused because it is in status '%s'"
                            .formatted(gameId, session.getStatus())
            );
        }

        session.setStatus(GameStatus.PAUSED);
        gameSessionRepository.save(session);

        Section currentSection = bookService.getSection(session.getBookId(), session.getCurrentSectionId());
        return new GameMoveResult(session, null, currentSection);
    }

    public GameMoveResult resumeGame(String gameId) {
        GameSession session = getSession(gameId);

        if (session.getStatus() != GameStatus.PAUSED) {
            throw new IllegalStateException(
                    "Game '%s' cannot be resumed because it is in status '%s'"
                            .formatted(gameId, session.getStatus())
            );
        }

        session.setStatus(GameStatus.IN_PROGRESS);
        gameSessionRepository.save(session);

        Section currentSection = bookService.getSection(session.getBookId(), session.getCurrentSectionId());
        return new GameMoveResult(session, null, currentSection);
    }
}