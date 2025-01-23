package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.enums.Phase;

import java.util.List;
import java.util.UUID;

public record RoundDto(UUID id, Phase phase, int dealerIndex, List<TurnDto> turns, List<CardDto> communityCards, List<CardDto> deck, GameDto game) {
}
