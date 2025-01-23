package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record WinnerDto(UUID playerId, String username, int money, String avatar) {
}
