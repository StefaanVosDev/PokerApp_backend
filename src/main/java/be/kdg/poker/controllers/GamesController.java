package be.kdg.poker.controllers;


import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.controllers.dto.GameMessageDto;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.services.GameRoundService;
import be.kdg.poker.services.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/games")
public class GamesController {

    private final GameService gameService;
    private final GameRoundService gameRoundService;

    public GamesController(GameService gameService, GameRoundService gameRoundService) {
        this.gameService = gameService;
        this.gameRoundService = gameRoundService;
    }

    @GetMapping("/{gameId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<GameDto> getGame(@PathVariable UUID gameId) {
        log.info("Received request to get game");
        try {
            GameDto game = gameService.getGame(gameId);
            log.info("Successfully retrieved game with id: {}", gameId);
            return ResponseEntity.ok(game);
        } catch (GameNotFoundException e) {
            log.error("Error retrieving game", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<GameDto>> getGames() {
        log.info("Received request to get all game");
        List<GameDto> games = gameService.getGames();
        log.info("Successfully retrieved all the games");
        log.info("Returning {} games", games.size());
        return ResponseEntity.ok(games);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<GameDto> createGame(@RequestBody GameDto gameDto) {
        log.info("Received request to create a new game");
        GameDto game = gameRoundService.createGame(gameDto);
        log.info("Successfully created a new game with id: {}", game.id());
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/join")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> joinGame(@PathVariable UUID gameId) {
        gameRoundService.addLoggedInPlayerToGame(gameId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}/isOnMove")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Boolean> isLoggedInUserOnMove(@PathVariable UUID gameId) {
        boolean isOnMove = gameRoundService.isLoggedInUserOnMove(gameId);
        return ResponseEntity.ok(isOnMove);
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<GameMessageDto>> getMessages(@RequestParam UUID gameId) {
        log.info("Received request to get messages for game with id: {}", gameId);
        List<GameMessageDto> messages = gameService.getMessages(gameId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{gameId}/addMessage")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> addMessage(@PathVariable UUID gameId, @RequestParam String message) {
        log.info("Received request to add message to game with id: {}", gameId);
        return gameService.addMessage(gameId, message);

    }

    @PutMapping("/{gameId}/status")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> startGame(@PathVariable UUID gameId) {
        log.info("Received request to start game with id: {}", gameId);
        gameRoundService.startGame(gameId);
        return ResponseEntity.ok().build();
    }
}