package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.CurrentTurnDto;
import be.kdg.poker.controllers.dto.TurnDto;
import be.kdg.poker.domain.Turn;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.services.GameService;
import be.kdg.poker.services.PlayerService;
import be.kdg.poker.services.RoundService;
import be.kdg.poker.services.TurnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/turns")
@Slf4j
public class TurnsController {
    private final TurnService turnService;
    private final GameService gameService;
    private final RoundService roundService;
    private final PlayerService playerService;

    public TurnsController(TurnService turnService, GameService gameService, RoundService roundService, PlayerService playerService) {
        this.turnService = turnService;
        this.gameService = gameService;
        this.roundService = roundService;
        this.playerService = playerService;
    }

    @PutMapping("/{turnId}/checkAndMove")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> checkAndMove(@PathVariable UUID turnId, @RequestParam UUID gameId, @RequestParam UUID roundId) {
        log.info("Received request to check and move for turn with id: {}", turnId);
        try {
            roundService.handleTurnAction(turnId, gameId, roundId, turnService::check);
            return ResponseEntity.ok().build();
        } catch (TurnNotFoundException e) {
            log.error("Error executing action for turn with id: {}", turnId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{turnId}/foldAndMove")
    public ResponseEntity<Void> foldAndMove(@PathVariable UUID turnId, @RequestParam UUID gameId, @RequestParam UUID roundId) {
        log.info("Received request to fold and move for turn with id: {}", turnId);
        try {
            roundService.handleTurnAction(turnId, gameId, roundId, turnService::fold);
            return ResponseEntity.ok().build();
        } catch (TurnNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{turnId}/callAndMove")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> callAndMove(@PathVariable UUID turnId, @RequestParam UUID gameId, @RequestParam UUID roundId, @RequestParam int amount) {
        log.info("Received request to call and move for turn with id: {}", turnId);
        try {
            roundService.handleTurnAction(turnId, gameId, roundId, turn -> {
                turnService.call(turn, amount);
                playerService.updatePlayerMoney(turn.getId(), -amount);
            });
            return ResponseEntity.ok().build();
        } catch (TurnNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{turnId}/raiseAndMove")
    public ResponseEntity<Void> raiseAndMove(@PathVariable UUID turnId, @RequestParam UUID gameId, @RequestParam UUID roundId, @RequestParam int amount) {
        log.info("Received request to raise and move for turn with id: {}", turnId);
        try {
            roundService.handleTurnAction(turnId, gameId, roundId, turn -> {
                turnService.raise(turn, amount);
                playerService.updatePlayerMoney(turn.getId(), -amount);
            });
            return ResponseEntity.ok().build();
        } catch (TurnNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{turnId}/allinAndMove")
    public ResponseEntity<Void> allInAndMove(@PathVariable UUID turnId, @RequestParam UUID gameId, @RequestParam UUID roundId) {
        log.info("Received request to go all in and move for turn with id: {}", turnId);
        try {
            roundService.handleTurnAction(turnId, gameId, roundId, turn -> {
                var player = turn.getPlayer();
                var amount = player.getMoney();
                turnService.allin(turn, amount);
                playerService.updatePlayerMoney(turn.getId(), -amount);
            });
            return ResponseEntity.ok().build();
        } catch (TurnNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<CurrentTurnDto> getCurrentTurn(@RequestParam UUID gameId) {
        log.info("Received request to get current turn for game with id: {}", gameId);
        return gameService.getByIdWithRounds(gameId)
                .flatMap(game -> roundService.getLatestByGameWithTurns(gameId)
                        .map(round -> {
                            if (round.getPhase() == Phase.FINISHED) {
                                log.info("Game with id: {} is finished", gameId);
                                return ResponseEntity.ok().<CurrentTurnDto>build();
                            }

                            var turns = round.getTurns();
                            if (turns == null || turns.isEmpty()) {
                                log.error("No turns found for game with id: {}", gameId);
                                return ResponseEntity.notFound().<CurrentTurnDto>build();
                            }
                            var latestTurn = turns.stream()
                                    .filter(turn -> turn.getMoveMade() == PlayerStatus.ON_MOVE)
                                    .findFirst()
                                    .orElseThrow(() -> new TurnNotFoundException("No turn found"));

                            log.info("Successfully retrieved current turn for game with id: {}", gameId);
                            return ResponseEntity.ok(new CurrentTurnDto(latestTurn.getId(), latestTurn.getCreatedAt()));
                        })
                )
                .orElseGet(() -> {
                    log.error("Error retrieving current turn for game with id: {}", gameId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/round")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<TurnDto>> getTurnsByRoundId(@RequestParam UUID roundId) {
        log.info("Received request to get turns for round with id: {}", roundId);
        List<Turn> turns = turnService.getAllByRoundIdWithPlayer(roundId);
        var turnDtoList = turns.stream()
                .map(turnService::mapToDtoWithPlayer)
                .toList();
        log.info("Successfully retrieved turns for round with id: {}", roundId);
        return ResponseEntity.ok(turnDtoList);
    }

    @GetMapping("/{id}/timeRemaining")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Integer> getTimeRemaining(@PathVariable UUID id) {
        return turnService.calculateTimeRemaining(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
