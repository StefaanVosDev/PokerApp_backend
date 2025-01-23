package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.PlayerDto;
import be.kdg.poker.controllers.dto.PlayerGameDto;
import be.kdg.poker.controllers.dto.WinnerDto;
import be.kdg.poker.domain.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PlayerServiceUnitTest {

    @Autowired
    private PlayerService playerService;

    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private TurnRepository turnRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private RoundRepository roundRepository;


    private Player mockPlayer;
    private Round mockRound;
    private Turn mockTurn;
    private UUID turnId;
    private UUID gameId;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        turnId = UUID.randomUUID();
        mockPlayer = new Player();
        mockPlayer.setId(UUID.randomUUID());
        mockPlayer.setMoney(1000);

        mockTurn = new Turn(mockPlayer);
        mockTurn.setId(turnId);

        mockRound = new Round();
        mockRound.setId(UUID.randomUUID());

        gameId = UUID.randomUUID();
        mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setStatus(GameStatus.IN_PROGRESS);

        mockTurn.setMoveMade(PlayerStatus.ON_MOVE);
        mockGame.setPlayers(List.of(mockPlayer));
    }

    @Test
    void updatePlayerMoney_ShouldUpdateMoney_WhenTurnExists() {
        // Arrange
        when(turnRepository.findByIdWithPlayer(turnId)).thenReturn(Optional.of(mockTurn));

        // Act
        playerService.updatePlayerMoney(turnId, 500);

        // Assert
        assertEquals(1500, mockPlayer.getMoney());
        verify(playerRepository, times(1)).save(mockPlayer);
    }

    @Test
    void updatePlayerMoney_ShouldThrowTurnNotFoundException_WhenTurnDoesNotExist() {
        // Arrange
        when(turnRepository.findByIdWithPlayer(turnId)).thenReturn(Optional.empty());

        // Act & Assert
        TurnNotFoundException exception = assertThrows(TurnNotFoundException.class, () -> playerService.updatePlayerMoney(turnId, 500));
        assertEquals("Turn not found", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void mapToDto_ShouldReturnPlayerDto_WithCorrectValues() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        int playerMoney = 1000;
        Player player = new Player();
        player.setId(playerId);
        player.setMoney(playerMoney);

        // Act
        PlayerDto playerDto = playerService.mapToDto(player);

        // Assert
        assertEquals(playerId, playerDto.id());
        assertEquals(playerMoney, playerDto.money());
    }

    @Test
    void getWinner_ShouldReturnWinnerDto_WithCorrectValues() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Player player = new Player();
        player.setId(playerId);
        player.setMoney(1000);
        Account account = new Account();
        account.setUsername("TestAccount for player");
        player.setAccount(account);

        when(playerRepository.findByIdWithAccount(playerId)).thenReturn(Optional.of(player));

        // Act
        WinnerDto winnerDto = playerService.getWinner(playerId);

        // Assert
        assertEquals(playerId, winnerDto.playerId());
        assertEquals(1000, winnerDto.money());
    }

    @Test
    void getWinner_ShouldThrowPlayerNotFoundException_WhenPlayerDoesNotExist() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findByIdWithAccount(playerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlayerNotFoundException.class, () -> playerService.getWinner(playerId));
    }
    @Test
    void getCurrentPlayerOnMove_ShouldReturnPlayerDto_WhenPlayerIsOnMove() {
        // Arrange
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findLatestByGame(mockGame.getId())).thenReturn(Optional.of(mockRound));
        when(turnRepository.findAllByPlayerIdAndRoundId(mockPlayer.getId(), mockRound.getId())).thenReturn(List.of(mockTurn));

        // Act
        PlayerDto result = playerService.getCurrentPlayerOnMove(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(mockPlayer.getId(), result.id());
    }

    @Test
    void getCurrentPlayerOnMove_ShouldThrowGameNotFoundException_WhenGameDoesNotExist() {
        // Arrange
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GameNotFoundException.class, () -> playerService.getCurrentPlayerOnMove(gameId));
    }

    @Test
    void getCurrentPlayerOnMove_ShouldReturnNull_WhenGameIsNotInProgress() {
        // Arrange
        mockGame.setStatus(GameStatus.FINISHED);
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));

        // Act
        PlayerDto result = playerService.getCurrentPlayerOnMove(gameId);

        // Assert
        assertNull(result);
    }

    @Test
    void getCurrentPlayerOnMove_ShouldReturnRoundNotFoundException_WhenRoundDoesNotExist() {
        // Arrange
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findLatestByGame(mockGame.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoundNotFoundException.class, () -> playerService.getCurrentPlayerOnMove(gameId));
    }

    @Test
    void getCurrentPlayersOnMove_ShouldReturnPlayerGameDtoList_WhenPlayersAreOnMove() {
        // Arrange
        when(gameRepository.findAllGamesWithPlayers()).thenReturn(List.of(mockGame));
        when(roundRepository.findLatestByGame(mockGame.getId())).thenReturn(Optional.of(mockRound));
        when(turnRepository.findAllByPlayerIdAndRoundId(mockPlayer.getId(), mockRound.getId())).thenReturn(List.of(mockTurn));

        // Act
        List<PlayerGameDto> result = playerService.getCurrentPlayersOnMove();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockGame.getId(), result.get(0).gameId());
        assertEquals(mockPlayer.getId(), result.get(0).playerOnMove().id());
    }

    @Test
    void getCurrentPlayersOnMove_ShouldReturnEmptyList_WhenNoGamesExist() {
        // Arrange
        when(gameRepository.findAllGamesWithPlayers()).thenReturn(List.of());

        // Act
        List<PlayerGameDto> result = playerService.getCurrentPlayersOnMove();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentPlayersOnMove_ShouldReturnNullPlayer_WhenGameIsNotInProgress() {
        // Arrange
        mockGame.setStatus(GameStatus.FINISHED);
        when(gameRepository.findAllGamesWithPlayers()).thenReturn(List.of(mockGame));

        // Act
        List<PlayerGameDto> result = playerService.getCurrentPlayersOnMove();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockGame.getId(), result.get(0).gameId());
        assertNull(result.get(0).playerOnMove());
    }

    @Test
    void getCurrentPlayersOnMove_ShouldReturnNullPlayer_WhenRoundDoesNotExist() {
        // Arrange
        when(gameRepository.findAllGamesWithPlayers()).thenReturn(List.of(mockGame));
        when(roundRepository.findLatestByGame(mockGame.getId())).thenReturn(Optional.empty());

        // Act
        List<PlayerGameDto> result = playerService.getCurrentPlayersOnMove();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockGame.getId(), result.get(0).gameId());
        assertNull(result.get(0).playerOnMove());
    }
}