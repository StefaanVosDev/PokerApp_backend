package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.enums.Suit;

import java.util.UUID;

public record CardDto(UUID id, Suit suit, int rank) {

}