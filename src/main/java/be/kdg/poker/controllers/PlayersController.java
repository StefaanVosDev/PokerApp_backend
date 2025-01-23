package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.controllers.dto.PlayerGameDto;
import be.kdg.poker.controllers.dto.WinnerDto;
import be.kdg.poker.services.GameService;
import be.kdg.poker.services.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/players")
public class PlayersController {

    private final PlayerService playerService;
    private final GameService gameService;

    public PlayersController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @GetMapping("/winner")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<WinnerDto> getWinner(@RequestParam UUID winnerId) {
        log.info("Received request to get winner");

        WinnerDto winnerDto = playerService.getWinner(winnerId);
        log.info("Successfully got winner");
        gameService.endGame(winnerId);
        return ResponseEntity.ok(winnerDto);
    }

    @GetMapping("/{gameId}/playerOnMove")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<PlayerDto> getPlayerOnMove(@PathVariable UUID gameId) {
        PlayerDto player = playerService.getCurrentPlayerOnMove(gameId);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/playersOnMove")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<PlayerGameDto>> getPlayersOnMove() {
        List<PlayerGameDto> players = playerService.getCurrentPlayersOnMove();
        return ResponseEntity.ok(players);
    }
}
