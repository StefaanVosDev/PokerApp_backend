package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.domain.enums.Suit;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class GameRoundServiceUnitTest {
    @Autowired
    private GameRoundService gameRoundService;

    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private RoundRepository roundRepository;
    @MockBean
    private TurnService turnService;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private AccountService accountService;
    @MockBean
    private TurnRepository turnRepository;


    private Game mockGame;
    private Round mockRound;
    private UUID gameId;
    private UUID roundId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        roundId = UUID.randomUUID();

        mockGame = new Game();
        mockGame.setId(gameId);

        mockRound = new Round();
        mockRound.setId(roundId);
        mockRound.setPhase(Phase.PRE_FLOP);
        mockRound.setDeck(new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 10),
                new Card(Suit.CLUBS, 2),
                new Card(Suit.DIAMONDS, 5),
                new Card(Suit.SPADES, 14),
                new Card(Suit.HEARTS, 8)
        )));
        mockRound.setCommunityCards(new ArrayList<>());
        mockRound.setGame(mockGame);
        mockGame.setRounds(new ArrayList<>(List.of(mockRound)));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(mockRound));
    }

    @Test
    void create_ShouldReturnRound_WhenSaveIsSuccessful() {
        // Arrange
        when(roundRepository.save(any(Round.class))).thenReturn(mockRound);

        // Act
        var result = gameRoundService.create(any(Game.class));

        // Assert
        assertEquals(mockRound, result);
        verify(roundRepository, times(1)).save(any(Round.class));
    }

    @Test
    void initiateDeck_ShouldReturnACompleteShuffledDeck() {
        // Act
        List<Card> deck = gameRoundService.initiateDeck();

        // Assert
        assertNotNull(deck);
        assertEquals(52, deck.size());
        assertTrue(deck.stream().allMatch(card -> card.getSuit() != null && card.getRank() >= 2 && card.getRank() <= 14));
    }

    @Test
    void assignPlayerHand_ShouldAssignCardsToPlayers() {
        Player player1 = new Player(100);
        Player player2 = new Player(100);

        List<Card> mockDeck = new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 10),
                new Card(Suit.CLUBS, 2),
                new Card(Suit.DIAMONDS, 5),
                new Card(Suit.SPADES, 14),
                new Card(Suit.HEARTS, 8)
        ));
        mockRound.setDeck(mockDeck);
        mockGame.setPlayers(List.of(player1, player2));

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithDeck(mockGame)).thenReturn(Optional.of(mockRound));

        gameRoundService.assignPlayerHand(gameId, mockRound);

        assertEquals(2, player1.getHand().size());
        assertEquals(2, player2.getHand().size());
        verify(playerRepository, times(2)).save(any(Player.class));
    }

    @Test
    void assignPlayerHand_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findByIdWithPlayers(any(UUID.class))).thenReturn(Optional.empty());

        Executable executable = () -> gameRoundService.assignPlayerHand(UUID.randomUUID(), new Round());

        assertThrows(GameNotFoundException.class, executable);

        try {
            executable.execute();
        } catch (Throwable e) {
            assertEquals("Game not found while trying to assign player hand", e.getMessage());
        }
    }

    @Test
    void assignPlayerHand_ShouldOnlyAssignCardsToPlayersNotBroke() {
        Player player1 = new Player(100);
        Player player2 = new Player(0);

        List<Card> mockDeck = new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 10),
                new Card(Suit.CLUBS, 2),
                new Card(Suit.DIAMONDS, 5),
                new Card(Suit.SPADES, 14),
                new Card(Suit.HEARTS, 8)
        ));
        mockRound.setDeck(mockDeck);
        mockGame.setPlayers(List.of(player1, player2));

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithDeck(mockGame)).thenReturn(Optional.of(mockRound));


        gameRoundService.assignPlayerHand(gameId, mockRound);

        assertEquals(2, player1.getHand().size());
        assertEquals(0, player2.getHand().size());
    }

    @Test
    void createAndSavePlayers_shouldAssignAttributesBasedOnGameSettings() {
        //ARRANGE
        var mockSettings = new Configuration();
        mockSettings.setTimer(false);
        mockSettings.setSmallBlind(15);
        mockSettings.setBigBlind(25);
        mockSettings.setId(UUID.randomUUID());
        mockSettings.setStartingChips(1500);
        mockGame.setSettings(mockSettings);

        when(accountService.getLoggedInUserEmail()).thenReturn("kaas");
        when(accountRepository.findAccountByEmail(any(String.class))).thenReturn(Optional.of(new Account()));

        //ACT
        gameRoundService.createAndSaveLoggedInPlayer(mockGame);

        //ASSERT
        var mockPlayers = mockGame.getPlayers();
        for (Player mockPlayer : mockPlayers) {
            assertEquals(1500, mockPlayer.getMoney());
        }
    }

    @Test
    void removeBrokePlayers_ShouldRemovePlayersWithZeroMoney() {
        UUID playerId1 = UUID.randomUUID();
        UUID playerId2 = UUID.randomUUID();
        Player player1 = new Player(playerId1, 100);
        Player player2 = new Player(playerId2, 0);

        List<Player> players = new ArrayList<>(List.of(player1, player2));
        mockGame.setPlayers(players);

        when(gameRepository.findByIdWithPlayers(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByIdWithGame(any(UUID.class))).thenReturn(Optional.of(mockRound));
        when(playerRepository.findByIdWithAccount(playerId1)).thenReturn(Optional.of(player1));
        when(playerRepository.findByIdWithAccount(playerId2)).thenReturn(Optional.of(player2));

        gameRoundService.removeAllBrokePlayersFromLastRound(roundId);

        assertEquals(1, mockGame.getPlayers().size());
        assertTrue(mockGame.getPlayers().contains(player1));
        assertFalse(mockGame.getPlayers().contains(player2));
    }

    @Test
    void createAndSaveInitialRound_ShouldCreateAndSaveInitialRound() {
        // Arrange
        Player player1 = new Player();
        Player player2 = new Player();
        Player player3 = new Player();
        player1.setId(UUID.randomUUID());
        player2.setId(UUID.randomUUID());
        player3.setId(UUID.randomUUID());

        mockGame.setPlayers(new ArrayList<>(List.of(player1, player2, player3)));
        when(roundRepository.save(any(Round.class))).thenReturn(mockRound);
        doNothing().when(turnService).createAndSaveInitialTurns(any(Round.class),any(Game.class));

        // Act
        Round result = gameRoundService.createInitialRound(mockGame);

        // Assert
        assertEquals(Phase.PRE_FLOP, result.getPhase());
        assertEquals(result.getGame(), mockGame);
        verify(roundRepository, times(1)).save(any(Round.class));
    }

    @Test
    void createGame_ShouldCreateGame() {
        // Arrange
        var mockSettings = new Configuration();
        mockSettings.setBigBlind(10);
        mockSettings.setSmallBlind(5);
        mockSettings.setTimer(false);
        mockSettings.setStartingChips(1000);

        GameDto gameDto = new GameDto(mockGame.getId(), GameStatus.WAITING, 4, new ArrayList<>(), new ArrayList<>(), null, "TestGame", mockSettings);
        mockGame.setName(gameDto.name());
        mockGame.setMaxPlayers(gameDto.maxPlayers());
        mockGame.setRounds(new ArrayList<>());
        mockGame.setPlayers(new ArrayList<>());
        mockGame.setSettings(mockSettings);

        when(gameRepository.saveAndFlush(any(Game.class))).thenReturn(mockGame);
        when(gameRepository.save(any(Game.class))).thenReturn(mockGame);
        when(accountRepository.findAccountByEmail(anyString())).thenReturn(Optional.of(new Account()));
        when(accountService.getLoggedInUserEmail()).thenReturn("player1");
        when(gameRepository.findByIdWithPlayers(any(UUID.class))).thenReturn(Optional.of(mockGame));

        // Act
        GameDto result = gameRoundService.createGame(gameDto);

        // Assert
        assertEquals(mockGame.getId(), result.id());
        assertEquals(mockGame.getStatus(), result.status());
        assertEquals(mockGame.getMaxPlayers(), result.maxPlayers());
        assertEquals(mockGame.getName(), result.name());
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void AddLoggedInPlayerToGame_ShouldAddPlayerToGame() {
        // Arrange
        var mockSettings = new Configuration();
        mockSettings.setBigBlind(10);
        mockSettings.setSmallBlind(5);
        mockSettings.setTimer(false);
        mockSettings.setStartingChips(1000);
        mockSettings.setId(UUID.randomUUID());

        Player player1 = new Player();
        player1.setId(UUID.randomUUID());
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("player2");

        mockGame.setPlayers(new ArrayList<>(List.of(player1)));
        mockGame.setSettings(mockSettings);

        when(gameRepository.findByIdWithSettings(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("player2");
        when(accountRepository.findAccountByEmail("player2")).thenReturn(Optional.of(account));
        when(playerRepository.save(any(Player.class))).thenReturn(new Player());
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));

        // Act
        gameRoundService.addLoggedInPlayerToGame(gameId);

        // Assert
        assertTrue(mockGame.getPlayers().stream().anyMatch(player -> "player2".equals(player.getUsername())));
    }

    @Test
    void AddLoggedInPlayerToGame_ShouldReturnAccountNotFoundException_GivenInvalidLoggedInPlayer() {
        // Arrange
        when(accountService.getLoggedInUserEmail()).thenReturn("player2");
        when(accountRepository.findAccountByEmail("player2")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> gameRoundService.addLoggedInPlayerToGame(gameId));
    }

    @Test
    void AddLoggedInPlayerToGame_ShouldReturnIllegalStateException_GivenAccountAlreadyInGame() {
        // Arrange
        Player player1 = new Player();
        player1.setId(UUID.randomUUID());
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("player1");
        player1.setAccount(account);

        mockGame.setPlayers(new ArrayList<>(List.of(player1)));

        when(gameRepository.findByIdWithSettings(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("player1");
        when(accountRepository.findAccountByEmail("player1")).thenReturn(Optional.of(account));
        when(playerRepository.save(any(Player.class))).thenReturn(new Player());
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> gameRoundService.addLoggedInPlayerToGame(gameId));
    }

    @Test
    void isLoggedInUserOnMove_ShouldReturnTrue_WhenUserIsOnMove() {
        // Arrange
        Player player1 = new Player();
        player1.setId(UUID.randomUUID());
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("player1");
        account.setPlayers(new ArrayList<>(List.of(player1)));
        player1.setAccount(account);

        Player player2 = new Player();
        player2.setId(UUID.randomUUID());
        Account account2 = new Account();
        account2.setId(UUID.randomUUID());
        account2.setUsername("player2");
        account2.setPlayers(new ArrayList<>(List.of(player2)));
        player2.setAccount(account2);

        Turn turn1 = new Turn();
        turn1.setMoveMade(PlayerStatus.CALL);
        turn1.setPlayer(player1);
        Turn turn2 = new Turn();
        turn2.setMoveMade(PlayerStatus.ON_MOVE);
        turn2.setPlayer(player2);

        player1.setTurns(new ArrayList<>(List.of(turn1)));
        player2.setTurns(new ArrayList<>(List.of(turn2)));

        mockGame.setPlayers(new ArrayList<>(List.of(player1, player2)));
        when(gameRepository.findByIdWithRounds(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("player1");
        when(accountRepository.findAccountByEmail("player1")).thenReturn(Optional.of(account));
        when(accountRepository.findAccountByEmail("player2")).thenReturn(Optional.of(account2));
        when(turnRepository.findLastTurnByRoundIdWithPlayer(any(UUID.class))).thenReturn(Optional.of(turn1));

        // Act
        boolean result = gameRoundService.isLoggedInUserOnMove(gameId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isLoggedInUserOnMove_ShouldReturnFalse_WhenUserIsNotOnMove() {
        // Arrange
        Player player1 = new Player();
        player1.setId(UUID.randomUUID());
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("player1");
        account.setPlayers(new ArrayList<>(List.of(player1)));
        player1.setAccount(account);

        Player player2 = new Player();
        player2.setId(UUID.randomUUID());
        Account account2 = new Account();
        account2.setId(UUID.randomUUID());
        account2.setUsername("player2");
        account2.setPlayers(new ArrayList<>(List.of(player2)));
        player2.setAccount(account2);

        Turn turn1 = new Turn();
        turn1.setMoveMade(PlayerStatus.CALL);
        turn1.setPlayer(player1);
        Turn turn2 = new Turn();
        turn2.setMoveMade(PlayerStatus.ON_MOVE);
        turn2.setPlayer(player2);

        player1.setTurns(new ArrayList<>(List.of(turn1)));
        player2.setTurns(new ArrayList<>(List.of(turn2)));

        mockGame.setPlayers(new ArrayList<>(List.of(player1, player2)));
        when(gameRepository.findByIdWithRounds(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("player1");
        when(accountRepository.findAccountByEmail("player1")).thenReturn(Optional.of(account));
        when(accountRepository.findAccountByEmail("player2")).thenReturn(Optional.of(account2));
        when(turnRepository.findLastTurnByRoundIdWithPlayer(any(UUID.class))).thenReturn(Optional.of(turn2));

        // Act
        boolean result = gameRoundService.isLoggedInUserOnMove(gameId);

        // Assert
        assertFalse(result);
    }
    @Test
    void startGame_ShouldStartGameSuccessfully() {
        Player firstPlayer = new Player();
        firstPlayer.setPosition(0);
        Account account1 = new Account();
        account1.setEmail("test@example.com");
        firstPlayer.setAccount(account1);

        Player secondPlayer = new Player();
        secondPlayer.setPosition(1);
        Account account2 = new Account();
        account2.setEmail("second@example.com");
        secondPlayer.setAccount(account2);

        mockGame.setPlayers(new ArrayList<>(List.of(firstPlayer, secondPlayer)));

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("test@example.com");
        when(roundRepository.save(any(Round.class))).thenReturn(new Round());

        doAnswer(invocation -> {
            Game savedGame = invocation.getArgument(0);
            savedGame.setStatus(GameStatus.IN_PROGRESS);
            return savedGame;
        }).when(gameRepository).save(any(Game.class));

        gameRoundService.startGame(gameId);

        verify(gameRepository, times(1)).save(mockGame);
        assertEquals(GameStatus.IN_PROGRESS, mockGame.getStatus());
        verify(roundRepository, times(1)).save(any(Round.class));
    }

    @Test
    void startGame_ShouldThrowSecurityException_WhenUserIsNotFirstPlayer() {
        Player firstPlayer = new Player();
        firstPlayer.setPosition(0);
        Account account = new Account();
        account.setEmail("first@example.com");
        firstPlayer.setAccount(account);
        mockGame.setPlayers(new ArrayList<>(List.of(firstPlayer)));

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("test@example.com");

        SecurityException exception = assertThrows(SecurityException.class, () -> gameRoundService.startGame(gameId));
        assertEquals("Only the creator of the game can start the game", exception.getMessage());
    }

    @Test
    void startGame_ShouldThrowIllegalStateException_WhenOnlyOnePlayer() {
        Player firstPlayer = new Player();
        firstPlayer.setPosition(0);
        Account account = new Account();
        account.setEmail("test@example.com");
        firstPlayer.setAccount(account);
        mockGame.setPlayers(new ArrayList<>(List.of(firstPlayer)));

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(accountService.getLoggedInUserEmail()).thenReturn("test@example.com");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gameRoundService.startGame(gameId));
        assertEquals("Game cannot start with only one player", exception.getMessage());
    }
}