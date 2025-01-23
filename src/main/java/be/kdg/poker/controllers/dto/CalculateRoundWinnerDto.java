package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Player;

import java.util.List;
import java.util.Map;

public record CalculateRoundWinnerDto(List<Player> players, Map<Player, List<Card>> hands, Map<AllInDto, Integer> stakes) {
}
