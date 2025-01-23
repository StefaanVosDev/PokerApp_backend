package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurnDto(UUID id, PlayerStatus moveMade, int moneyGambled, PlayerDto player, UUID roundId, Phase madeInPhase, LocalDateTime createdAt) {
}
