package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AchievementNotificationDto(UUID id, String achievementName, String message, String accountUsername, LocalDateTime timestamp) {
}
