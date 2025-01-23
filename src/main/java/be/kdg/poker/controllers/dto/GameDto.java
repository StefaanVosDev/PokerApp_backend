package be.kdg.poker.controllers.dto;

import be.kdg.poker.domain.Configuration;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.enums.GameStatus;

import java.util.List;
import java.util.UUID;


public record GameDto(UUID id, GameStatus status, int maxPlayers, List<RoundDto> rounds, List<PlayerDto> players, Player winner, String name, Configuration settings) {
}