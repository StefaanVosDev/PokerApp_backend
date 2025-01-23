package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendRequestDto(UUID id, String requestingFriendUsername, String message, LocalDateTime timestamp) {
}
