package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record PlayerGameDto(UUID gameId, PlayerDto playerOnMove) {
}
