package be.kdg.poker.controllers.dto;

import java.util.UUID;

public record AvatarWithUsernameDto(UUID id, String image, String username) {
}
