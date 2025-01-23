package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.controllers.dto.PlayerGameDto;
import be.kdg.poker.controllers.dto.WinnerDto;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Round;
import be.kdg.poker.domain.Turn;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.PlayerNotFoundException;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TurnRepository turnRepository;
    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;

    public PlayerService(PlayerRepository playerRepository, TurnRepository turnRepository, GameRepository gameRepository, RoundRepository roundRepository) {
        this.playerRepository = playerRepository;
        this.turnRepository = turnRepository;
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
    }

    @Transactional
    public void updatePlayerMoney(UUID turnId, int money) {
        log.info("Updating player money with turn ID: {} and money: {}", turnId, money);
        var turn = turnRepository.findByIdWithPlayer(turnId)
                .orElseThrow(() -> {
                    log.error("Turn not found with ID: {}", turnId);
                    return new TurnNotFoundException("Turn not found");
                });
        var player = turn.getPlayer();
        player.setMoney(player.getMoney() + money);
        playerRepository.save(player);
        log.info("Player money updated with turn ID: {} and money: {}", turnId, money);
    }

    public PlayerDto getCurrentPlayerOnMove(UUID gameId) {
        var game = gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + gameId));


        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            Round currentRound = roundRepository.findLatestByGame(game.getId())
                    .orElseThrow(() -> new RoundNotFoundException("Round not found"));

            if (currentRound == null) {
                return null;
            }

            Player foundPlayer = game.getPlayers().stream()
                    .flatMap(player -> turnRepository.findAllByPlayerIdAndRoundId(player.getId(), currentRound.getId()).stream())
                    .filter(turn -> turn.getMoveMade() == PlayerStatus.ON_MOVE)
                    .map(Turn::getPlayer)
                    .findFirst()
                    .orElse(null);
            if (foundPlayer == null) {
                return null;
            }
            return mapToDto(foundPlayer);
        }
        return null;
    }

    public List<PlayerGameDto> getCurrentPlayersOnMove() {
        List<PlayerGameDto> playersOnMove = new ArrayList<>();

        List<Game> games = gameRepository.findAllGamesWithPlayers();

        for (Game game : games) {
            if (game.getStatus() == GameStatus.IN_PROGRESS) {
                Round currentRound = roundRepository.findLatestByGame(game.getId())
                        .orElse(null);

                if (currentRound != null) {
                    game.getPlayers().stream()
                            .flatMap(player -> turnRepository.findAllByPlayerIdAndRoundId(player.getId(), currentRound.getId()).stream())
                            .filter(turn -> turn.getMoveMade() == PlayerStatus.ON_MOVE)
                            .map(Turn::getPlayer)
                            .findFirst().ifPresent(foundPlayer -> playersOnMove.add(new PlayerGameDto(game.getId(), mapToDto(foundPlayer))));

                    continue;
                }
            }
            playersOnMove.add(new PlayerGameDto(game.getId(), null));
        }

        return playersOnMove;
    }



    public PlayerDto mapToDto(Player player) {
        return new PlayerDto(player.getId(), player.getMoney(), player.getUsername(), player.getPosition());
    }

    public WinnerDto getWinner(UUID playerId) {
        log.info("Fetching winner with player ID: {}", playerId);
        var player = playerRepository.findByIdWithAccount(playerId)
                .orElseThrow(() -> {
                    log.error("Player not found with ID: {}", playerId);
                    return new PlayerNotFoundException("Player not found");
                });
        return mapToWinnerDto(player);
    }

    private WinnerDto mapToWinnerDto(Player player) {
        return new WinnerDto(player.getId(), player.getAccount().getUsername(), player.getMoney(), "/src/assets/duckpfp.png");
    }
}
