package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.controllers.dto.GameMessageDto;
import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.events.game.GameResultEvent;
import be.kdg.poker.domain.events.game.GameSessionEvent;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.PlayerNotFoundException;
import be.kdg.poker.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameMessageRepository gameMessageRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TurnRepository turnRepository;
    private final EventService eventService;
    private final AchievementService achievementService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, GameMessageRepository gameMessageRepository, AccountService accountService, AccountRepository accountRepository, TurnRepository turnRepository, EventService eventService, AchievementService achievementService) {
        this.gameRepository = gameRepository;
        this.turnRepository = turnRepository;
        this.playerRepository = playerRepository;
        this.gameMessageRepository = gameMessageRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.eventService = eventService;
        this.achievementService = achievementService;
    }

    public GameDto getGame(UUID gameId) {
        log.info("Fetching game with ID: {}", gameId);
        var game = gameRepository.findByIdWithSettings(gameId).orElseThrow(() -> new GameNotFoundException("Game with ID: " + gameId + " not found"));

        var players = playerRepository.findAllByGameId(gameId);
        if (players.isEmpty()) throw new PlayerNotFoundException("game with ID: " + gameId + " has no players");
        game.setPlayers(players);

        for (Player player : game.getPlayers()) {
            player.setTurns(turnRepository.findAllByPlayerId(player.getId()));
        }

        log.info("Game with ID: {} found.", gameId);
        return mapToDto(game);
    }

    public List<GameDto> getGames() {
        log.info("Fetching all games");

        List<Game> games = gameRepository.findAllWithSettings();

        if (games.isEmpty()) {
            log.error("No games found");
            throw new GameNotFoundException("No games found");
        }

        log.info("All games found");

        List<GameDto> gameDtoList = new ArrayList<>();
        for (Game game : games) {
            log.info("Fetching players for game: {}", game.getId());
            var players = playerRepository.findAllByGameId(game.getId());
            if (players.isEmpty()) {
                log.error("Game with ID: {} has no players", game.getId());
                throw new PlayerNotFoundException("Game with ID: " + game.getId() + " has no players");
            }
            game.setPlayers(players);

            for (Player player : game.getPlayers()) {
                player.setTurns(turnRepository.findAllByPlayerId(player.getId()));
            }

            log.info("Mapping game to dto: {}", game);
            gameDtoList.add(mapToDto(game));
        }
        return gameDtoList;
    }

    private GameDto mapToDto(Game game) {
        return new GameDto(
                game.getId(),
                game.getStatus(),
                game.getMaxPlayers(),
                null,
                game.getPlayers().stream().map(this::mapPlayerToDto).toList(),
                null,
                game.getName(),
                game.getSettings()
        );
    }

    private PlayerDto mapPlayerToDto(Player player) {
        return new PlayerDto(player.getId(), player.getMoney(), player.getUsername(), player.getPosition());
    }

    public Optional<Game> getByIdWithRounds(UUID gameId) {
        return gameRepository.findByIdWithRounds(gameId);
    }

    public Optional<Game> getByIdWithPlayers(UUID gameId) {
        return gameRepository.findByIdWithPlayers(gameId);
    }

    @Transactional
    public void endGame(UUID winnerId) {
        log.info("Ending game with winner ID: {}", winnerId);

        // Haal het spel en de winnaar op
        var playerWithGame = playerRepository.findByIdWithGame(winnerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + winnerId));
        var game = gameRepository.findById(playerWithGame.getGame().getId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with ID: " + winnerId));

        var winnerAccount = accountRepository.findByUsernameWithAvatars(playerWithGame.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Account not found for username: " + playerWithGame.getUsername()));
        if (game.getStatus() != GameStatus.FINISHED) {
            winnerAccount.getCountersAchievements().put("wins", winnerAccount.getCountersAchievements().getOrDefault("wins", 0) + 1);
        }
        accountRepository.save(winnerAccount);

        if (game.getRounds().isEmpty() || game.getRounds().stream().allMatch(round -> round.getPhase() == Phase.PRE_FLOP)) {
            log.error("Game with ID: {} has no valid rounds, cannot end game", game.getId());
        } else {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(playerRepository.findById(winnerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + winnerId)));
            gameRepository.save(game);

            List<Achievement> unlockedAchievements = achievementService.checkForAchievementsAtEndOfGame(game);

            log.info("Game ended with winner ID: {}. Unlocked achievements: {}", winnerId,
                    unlockedAchievements.stream().map(Achievement::getName).toList());

            // Verstuur `game_ended` event enkel voor de winnaar
            GameSessionEvent gameEndedEvent = new GameSessionEvent(
                    winnerAccount.getId().toString(), // User ID
                    game.getId().toString(),               // Game ID
                    "game_ended",                          // Event type
                    LocalDateTime.now()                    // Timestamp
            );
            eventService.sendGameSessionEvent(gameEndedEvent);
            log.info("GameSessionEvent 'game_ended' sent for winner with ID: {} in game with ID: {}", winnerAccount.getId(), game.getId());

            // Verstuur `game_result` event voor de winnaar
            GameResultEvent gameResultEvent = new GameResultEvent(
                    winnerAccount.getId().toString(), // User ID
                    game.getId().toString(),               // Game ID
                    true,
                    game.getWinner().getMoney(),// Win: true (want dit is de winnaar)
                    LocalDateTime.now());
            eventService.sendGameResultEvent(gameResultEvent);
            log.info("GameResultEvent 'game_result' sent for winner with ID: {} in game with ID: {}", winnerAccount.getId(), game.getId());
        }
    }


    public List<GameMessageDto> getMessages(UUID gameId) {
        log.info("Fetching messages for game with ID: {}", gameId);
        List<GameMessage> messages = gameMessageRepository.findAllByGameIdWithPlayers(gameId);
        log.info("Messages for game with ID: {} found", gameId);
        return messages.stream()
                .map(message -> new GameMessageDto(message.getId(), mapToPlayerDto(message.getSender()), message.getContent(), message.getTimestamp()))
                .toList();
    }

    public PlayerDto mapToPlayerDto(Player player) {
        return new PlayerDto(player.getId(), player.getMoney(), player.getUsername(), player.getPosition());
    }

    public ResponseEntity<Void> addMessage(UUID gameId, String message) {
        log.info("Adding message to game with ID: {}", gameId);
        var game = gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game with ID: " + gameId + " not found"));
        var loggedInPlayer = accountService.getLoggedInUserEmail();
        Account account = accountRepository.findAccountByEmail(loggedInPlayer)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with email: " + loggedInPlayer));
        Player currentPlayer = playerRepository.findByAccountAndGame(account.getId(), game.getId())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with email: " + loggedInPlayer + " and game ID: " + gameId));

        var gameMessage = new GameMessage(currentPlayer, message);
        gameMessage.setGame(game);
        gameMessageRepository.save(gameMessage);
        log.info("Message added to game with ID: {}", gameId);
        return ResponseEntity.ok().build();
    }
}
