package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.CardDto;
import be.kdg.poker.controllers.dto.RoundDto;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Round;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.InvalidWinnerException;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/rounds")
public class RoundsController {

    private final RoundService roundService;
    private final GameService gameService;
    private final DividePotService dividePotService;
    private final CardService cardService;
    private final GameRoundService gameRoundService;
    private final Map<String, List<Long>> requestMap = new HashMap<>();


    public RoundsController(RoundService roundService, GameService gameService, DividePotService dividePotService, CardService cardService, GameRoundService gameRoundService) {
        this.roundService = roundService;
        this.gameService = gameService;
        this.dividePotService = dividePotService;
        this.cardService = cardService;
        this.gameRoundService = gameRoundService;
    }

    @GetMapping("/communityCards")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<CardDto>> getCommunityCards(@RequestParam UUID gameId) {
        log.info("Received request to get community cards for game with id: {}", gameId);
        List<Card> communityCards = roundService.getCommunityCards(gameId);
        List<CardDto> communityCardDtoList = communityCards.stream()
                .map(cardService::mapToDto)
                .toList();
        log.info("Successfully retrieved community cards for game with id: {}", gameId);
        return ResponseEntity.ok(communityCardDtoList);
    }


    @GetMapping("/current")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<RoundDto> getCurrentRound(@RequestParam UUID gameId) {
        log.info("Received request to get current round for game with id: {}", gameId);
        return (roundService.getCurrentRound(gameId))
                .map(round -> {
                    log.info("Successfully retrieved current round for game with id: {}", gameId);
                    return ResponseEntity.ok(roundService.mapToDto(round));
                })
                .orElseGet(() -> {
                    log.error("Error retrieving current round for game with id: {}", gameId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("{id}/dividePot")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<UUID>> dividePot(@PathVariable UUID id) throws InvalidWinnerException, InterruptedException {
        try {
            return dividePotService.getCalculateRoundWinnerDto(id)
                            .map(calculation -> {
                                var playersByWinIndex = dividePotService.calculateWinners(calculation);
                                var playersWithEarning = dividePotService.dividePot(playersByWinIndex, calculation);
                                gameRoundService.removeAllBrokePlayersFromLastRound(id);
                                return ResponseEntity.ok(playersWithEarning.keySet().stream()
                                        .map(Player::getId)
                                        .toList());
                            })
                            .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RoundNotFoundException e) {
            log.error("round with id " + id + " does not exist");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/createNewRoundIfFinished")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> createNewRoundIfFinished(@RequestParam UUID gameId, @RequestParam UUID roundId) {
        log.info("Received request to create a new round if finished for game with id: {}", gameId);
        Game game = gameService.getByIdWithPlayers(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found while trying to create a new round if finished"));
        if (game.getPlayers().size() <= 1) {
            log.info("Problem creating a new round if finished for game with id: {}", gameId);
            return ResponseEntity.noContent().build();
        }

        Round round = roundService.createNewRoundIfFinished(game, roundId);
        if (round == null) {
            log.error("Error creating a new round if finished for game with id: {}", gameId);
            return ResponseEntity.notFound().build();
        }
        log.info("Successfully created a new round if finished for game with id: {}", gameId);
        return ResponseEntity.ok().build();
    }

}
