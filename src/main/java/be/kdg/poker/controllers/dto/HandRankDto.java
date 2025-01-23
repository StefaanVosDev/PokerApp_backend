package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Card;

import java.util.List;


public record HandRankDto(int score, List<Card> cardsWithCombination, List<Card> cardsWithoutCombination) {
}
