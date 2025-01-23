package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record AchievementDto(UUID id, String name, String description, int pokerPoints) {
}