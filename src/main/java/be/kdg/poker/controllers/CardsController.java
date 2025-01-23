package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.PlayersHandDto;
import be.kdg.poker.exceptions.PlayersHandNotFoundException;
import be.kdg.poker.services.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cards")
public class CardsController {
    private final CardService cardService;

    public CardsController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<PlayersHandDto> getPlayersHand(@PathVariable UUID playerId) {
        log.info("Received request to get hand for player with id: {}", playerId);
        try {
            PlayersHandDto playersHand = cardService.getPlayersHand(playerId);
            log.info("Successfully retrieved hand for player with id: {}", playerId);
            return ResponseEntity.ok(playersHand);
        } catch (PlayersHandNotFoundException e) {
            log.error("Error retrieving hand for player with id: {}", playerId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
