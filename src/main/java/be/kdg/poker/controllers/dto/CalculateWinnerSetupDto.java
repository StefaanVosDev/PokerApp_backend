package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Player;

import java.util.List;
import java.util.Map;

public record CalculateWinnerSetupDto(Map<Integer, List<Player>> scoreGroups, boolean hasTies, boolean onlyRoyalFlushTies) {
}
