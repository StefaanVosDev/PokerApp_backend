package be.kdg.poker.controllers.dto;

import be.kdg.poker.config.LocalDateDeserializer;
import be.kdg.poker.domain.Avatar;
import be.kdg.poker.domain.enums.Gender;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AccountDto(
        UUID id,
        String email,
        String username,
        String name,
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate age,
        String city,
        Gender gender,
        List<Avatar> ownedAvatars,
        Avatar activeAvatar,
        int wins,
        int playedGames,
        int royalFlushes,
        int flushes,
        int straights
) {
}
