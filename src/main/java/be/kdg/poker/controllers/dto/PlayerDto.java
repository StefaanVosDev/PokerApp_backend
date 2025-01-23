package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record PlayerDto(UUID id, int money, String username,int position) {
}
