package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.enums.Suit;

import java.util.EnumMap;
import java.util.HashMap;

public record CardPropsCountsDto(HashMap<Integer, Integer> rankCounts, EnumMap<Suit, Integer> suitCounts) {
}
