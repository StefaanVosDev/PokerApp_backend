package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(String sender, String receiver, String message, UUID gameId, LocalDateTime timestamp, boolean isRead) {
}
