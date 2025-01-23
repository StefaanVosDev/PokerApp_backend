package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record NotificationDto(UUID id, String message) {
}
