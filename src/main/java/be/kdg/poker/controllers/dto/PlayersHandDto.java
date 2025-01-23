package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Card;

import java.util.List;
import java.util.UUID;

public record PlayersHandDto(UUID playerId, UUID gameId, List<Card> hand, int score) {
}
