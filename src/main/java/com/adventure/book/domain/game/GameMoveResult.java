package com.adventure.book.domain.game;

import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.Section;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMoveResult {
    private GameSession gameSession;
    private Consequence appliedConsequence;
    private Section currentSection;
}
