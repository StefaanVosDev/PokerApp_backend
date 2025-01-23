package be.kdg.poker.controllers.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CurrentTurnDto(
        UUID id,
        LocalDateTime startTime) {
}
