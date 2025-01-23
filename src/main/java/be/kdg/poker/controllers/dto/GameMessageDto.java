package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameMessageDto(UUID id, PlayerDto player, String content, LocalDateTime timestamp) {
}
