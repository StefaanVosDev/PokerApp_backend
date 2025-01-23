package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.events.game.GameResultEvent;
import be.kdg.poker.domain.events.game.GameSessionEvent;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class GameRoundService {

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final CardRepository cardRepository;
    private final TurnService turnService;
    private final PlayerRepository playerRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ConfigurationRepository configurationRepository;
    private final TurnRepository turnRepository;
    private final EventService eventService;
    private final GameMessageRepository gameMessageRepository;

    public GameRoundService(GameRepository gameRepository, RoundRepository roundRepository, CardRepository cardRepository, TurnService turnService, PlayerRepository playerRepository, AccountService accountService, AccountRepository accountRepository, ConfigurationRepository configurationRepository, TurnRepository turnRepository, EventService eventService, GameMessageRepository gameMessageRepository) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.cardRepository = cardRepository;
        this.turnService = turnService;
        this.playerRepository = playerRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.configurationRepository = configurationRepository;
        this.turnRepository = turnRepository;
        this.eventService = eventService;
        this.gameMessageRepository = gameMessageRepository;
    }

    public List<Card> initiateDeck() {
        log.info("Initiating deck");
        List<Card> allCards = cardRepository.findAll();
        Collections.shuffle(allCards);
        log.info("Deck initiated");
        return allCards;
    }

    @Transactional
    public Round create(Game game) {
        log.info("Creating new round");
        List<Card> deck = initiateDeck();

        Round round = new Round(Phase.PRE_FLOP, deck, game);
        log.info("New round created");
        return roundRepository.save(round);
    }

    @Transactional
    public Round createInitialRound(Game game) {
        List<Card> deck = initiateDeck();
        Round initialRound = new Round(Phase.PRE_FLOP, deck, game);

        roundRepository.save(initialRound);
        turnService.createAndSaveInitialTurns(initialRound, game);

        return initialRound;
    }

    @Transactional
    public void createAndSaveLoggedInPlayer(Game game) {
        Account account = getLoggedInUser();

        Player player = createPlayer(account, game, 0);
        playerRepository.save(player);

        List<Player> players = new ArrayList<>();
        players.add(player);
        game.setPlayers(players);
    }

    private Account getLoggedInUser() {
        String email = accountService.getLoggedInUserEmail();
        return accountRepository.findAccountByEmail(email).orElseThrow(() -> {
            log.error("account with email {} not found", email);
            return new AccountNotFoundException("account with email " + email + " not found");
        });
    }

    private Player createPlayer(Account account, Game game, int position) {
        account.getCountersAchievements().put("playedGames", account.getCountersAchievements().getOrDefault("playedGames", 0) + 1);

        Player player = new Player();
        player.setMoney(game.getSettings().getStartingChips());
        player.setGame(game);
        player.setAccount(account);
        player.setPosition(position);
        player.setUsername(account.getUsername());
        return player;
    }

    @Transactional
    public GameDto createGame(GameDto gameDto) {
        log.info("Creating a new game with name: {} and maxPlayers: {} and settings: {}", gameDto.name(), gameDto.maxPlayers(), gameDto.settings());

        configurationRepository.save(gameDto.settings());

        Game game = new Game();
        game.setName(gameDto.name());
        game.setMaxPlayers(gameDto.maxPlayers());
        game.setStatus(GameStatus.WAITING);
        game.setRounds(new ArrayList<>());
        game.setSettings(gameDto.settings());

        game = gameRepository.saveAndFlush(game);

        createAndSaveLoggedInPlayer(game);

        game = gameRepository.save(game);

        log.info("New game created with ID: {}", game.getId());
        return mapToDto(game);
    }

    @Transactional
    public void startGame(UUID gameId) {
        Game game = gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found"));

        Player firstPlayer = game.getPlayers().stream()
                .min(Comparator.comparingInt(Player::getPosition))
                .orElseThrow(() -> new NoSuchElementException("No players found in the game"));

        String loggedInUser = accountService.getLoggedInUserEmail();

        if (!firstPlayer.getAccount().getEmail().equals(loggedInUser)) {
            log.warn("User with ID: {} is not the first player and cannot update the game status", loggedInUser);
            throw new SecurityException("Only the creator of the game can start the game");
        }
        if (game.getPlayers().size() == 1) {
            log.warn("Game cannot start with only one player");
            throw new IllegalStateException("Game cannot start with only one player");
        }

        Round initialRound = createInitialRound(game);
        game.getRounds().add(initialRound);
        assignPlayerHand(game.getId(), game.getRounds().get(0));

        if (game.getStatus() == GameStatus.WAITING) {
            game.setStatus(GameStatus.IN_PROGRESS);
            log.info("Game status changed to IN_PROGRESS for game ID: {}", gameId);

            // **GameSessionEvent versturen voor alle spelers**
            game.getPlayers().forEach(player -> {
                GameSessionEvent gameStartedEvent = new GameSessionEvent(
                        player.getAccount().getId().toString(), // User ID
                        game.getId().toString(),               // Game ID
                        "game_started",                        // Event type
                        LocalDateTime.now()                    // Timestamp
                );
                eventService.sendGameSessionEvent(gameStartedEvent);
                log.info("GameSessionEvent sent for player with ID: {} in game with ID: {}", player.getAccount().getId(), gameId);
            });
        } else {
            log.info("Game status not changed for game ID: {}. Current status: {}", gameId, game.getStatus());
        }

        gameRepository.save(game);
    }

    public void assignPlayerHand(UUID gameId, Round round) {
        log.info("Assigning player hand");
        Game game = gameRepository.findByIdWithPlayers(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found while trying to assign player hand"));

        List<Player> playersNotBroke = game.getPlayers()
                .stream()
                .filter(player -> player.getMoney() > 0)
                .toList();

        for (Player player : playersNotBroke) {
            giveAllPlayersTwoCardsFromTheDeck(round, player);
        }

        log.info("Players hands all got assigned");
    }

    @Transactional
    public void giveAllPlayersTwoCardsFromTheDeck(Round round, Player player) {
        log.info("Giving player with id {} two cards from the deck", player.getId());
        List<Card> playerCards = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            if (!round.getDeck().isEmpty()) {
                Card card = round.getDeck().get(0);
                round.getDeck().remove(card);
                playerCards.add(card);
            }
            log.info("Player with id {} got two cards from the deck", player.getId());
        }
        player.setHand(playerCards);
        playerRepository.save(player);
    }

    @Transactional
    public void addLoggedInPlayerToGame(UUID gameId) {
        Account account = getLoggedInUser();

        Game game = gameRepository.findByIdWithSettings(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found while trying to add player to table"));

        boolean isPlayerInGame = game.getPlayers().stream()
                .anyMatch(player -> player.getAccount() != null && player.getAccount().getId().equals(account.getId()));

        if (isPlayerInGame) {
            log.error("Account with email {} is already in the game", account.getEmail());
            throw new IllegalStateException("Account is already in the game");
        }

        Player player = createPlayer(account, game, game.getPlayers().size());
        playerRepository.save(player);

        game.getPlayers().add(player);
        gameRepository.save(game);
    }

    public boolean isLoggedInUserOnMove(UUID gameId) {
        Account account = getLoggedInUser();

        Game game = gameRepository.findByIdWithRounds(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found"));

        Round currentRound = game.getRounds().get(game.getRounds().size() - 1);
        Turn lastTurn = turnRepository.findLastTurnByRoundIdWithPlayer(currentRound.getId())
                .orElseThrow(() -> new TurnNotFoundException("Turn not found"));

        return lastTurn.getPlayer().getAccount().getId().equals(account.getId());
    }

    private GameDto mapToDto(Game game) {
        return new GameDto(
                game.getId(),
                game.getStatus(),
                game.getMaxPlayers(),
                null,
                game.getPlayers().stream().map(this::mapPlayerToDto).toList(),
                game.getWinner(),
                game.getName(),
                game.getSettings()
        );
    }

    private PlayerDto mapPlayerToDto(Player player) {
        return new PlayerDto(player.getId(), player.getMoney(), player.getUsername(), player.getPosition());
    }


    @Transactional
    public void removeAllBrokePlayersFromLastRound(UUID roundId) {
        Round round = roundRepository.findByIdWithGame(roundId)
                .orElseThrow(() -> new IllegalArgumentException("Round not found"));
        Game game = gameRepository.findByIdWithPlayers(round.getGame().getId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        Iterator<Player> playerIterator = game.getPlayers().iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            if (player.getMoney() == 0) {
                removeBrokePlayer(player, game, playerIterator);
            }
        }
        gameRepository.save(game);
    }

    @Transactional
    public void removeBrokePlayer(Player player, Game game, Iterator<Player> playerIterator) {
        player = playerRepository.findByIdWithAccount(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        var position = player.getPosition();
        for (var gamePlayer: game.getPlayers()) {
            if (gamePlayer.getPosition() > position){
                gamePlayer.setPosition(gamePlayer.getPosition() - 1);
                playerRepository.save(gamePlayer);
            }
        }

        List<GameMessage> gameMessages = gameMessageRepository.findAllByPlayerId(player.getId());
        for (GameMessage gameMessage : gameMessages) {
            gameMessage.setSender(null);
            gameMessageRepository.save(gameMessage);
        }

        player.setTurns(null);

        List<Turn> turns = turnRepository.findAllByPlayerId(player.getId());
        for (Turn turn : turns) {
            turn.setPlayer(null);
            turnRepository.save(turn);
        }

        Account account = player.getAccount();
        if (account != null) {
            account.getPlayers().remove(player);
            accountRepository.save(account);
        }

        player.setAccount(null);
        player.setGame(null);
        playerRepository.save(player);

        GameResultEvent gameResultEvent = new GameResultEvent(
                account != null ? account.getId().toString() : null,  // User ID
                game.getId().toString(),                             // Game ID
                false,                                               // Win: false (verliezer)
                0,                                                   // Winstbedrag: 0
                LocalDateTime.now()                                  // Timestamp
        );
        eventService.sendGameResultEvent(gameResultEvent);
        log.info("GameResultEvent 'game_result' sent for player with ID: {} in game with ID: {}",
                player.getId(), game.getId());

        // Verstuur `game_ended` event enkel voor de winnaar
        GameSessionEvent gameEndedEvent = new GameSessionEvent(
                account != null ? account.getId().toString() : null,  // User ID
                game.getId().toString(),               // Game ID
                "game_ended",                          // Event type
                LocalDateTime.now()                    // Timestamp
        );
        eventService.sendGameSessionEvent(gameEndedEvent);
        log.info("GameSessionEvent 'game_ended' sent for winner with ID: {} in game with ID: {}", player.getId(), game.getId());

        playerIterator.remove();
        playerRepository.delete(player);
    }
}
