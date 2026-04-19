package com.adventure.book.domain.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {
    private String id;
    private String bookId;
    private String currentSectionId;
    private int health;
    private GameStatus status;
}
