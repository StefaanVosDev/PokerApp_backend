package be.kdg.poker.controllers.dto;

import java.util.List;

public record CompareEqualCardsDto(int maxNonCombinationCards, List<Integer> p1NonCombinationRanks, List<Integer> p2NonCombinationRanks) {
}
