package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameNotificationDto(UUID id, GameDto game, String message, String accountUsername, LocalDateTime timestamp) {
}
