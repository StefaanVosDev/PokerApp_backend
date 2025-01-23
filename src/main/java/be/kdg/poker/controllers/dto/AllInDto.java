package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Player;

public record AllInDto(Player player, boolean allIn) {
}
