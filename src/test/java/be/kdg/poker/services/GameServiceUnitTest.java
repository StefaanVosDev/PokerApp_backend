package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.controllers.dto.GameMessageDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.GameMessage;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.GameMessageRepository;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class GameServiceUnitTest {

    @Autowired
    private GameService gameService;

    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private AccountService accountService;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private GameMessageRepository gameMessageRepository;

    @Test
    void getGameWithValidIdReturnsAMappedGameDto() {
        // Arrange
        UUID gameId = UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff00");
        Game game = new Game();
        game.setId(gameId);
        game.setStatus(GameStatus.WAITING);
        game.setMaxPlayers(4);
        game.setPlayers(List.of(
                new Player(UUID.randomUUID(), 1000),
                new Player(UUID.randomUUID(), 800)
        ));

        when(gameRepository.findByIdWithSettings(gameId)).thenReturn(Optional.of(game));
        when(playerRepository.findAllByGameId(gameId)).thenReturn(game.getPlayers());

        // Act
        GameDto result = gameService.getGame(gameId);

        // Assert
        assertNotNull(result);
        assertEquals(gameId, result.id());
        assertEquals(GameStatus.WAITING, result.status());
        assertEquals(4, result.maxPlayers());
        assertEquals(2, result.players().size());
        assertEquals(1000, result.players().get(0).money());
    }

    @Test
    void getGameWithInvalidIdReturnsAGameNotFoundException() {
        // Arrange
        UUID gameId = UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff01");
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.empty());

        // Act & Assert
        GameNotFoundException exception = assertThrows(
                GameNotFoundException.class,
                () -> gameService.getGame(gameId)
        );

        assertEquals("Game with ID: " + gameId + " not found", exception.getMessage());
    }

    @Test
    void getGamesReturnsAListOfMappedGameDtoList() {
        // Arrange
        Game game1 = new Game();
        game1.setId(UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff00"));
        game1.setStatus(GameStatus.WAITING);
        game1.setMaxPlayers(4);
        game1.setPlayers(List.of(
                new Player(UUID.randomUUID(), 1000),
                new Player(UUID.randomUUID(), 800)
        ));

        Game game2 = new Game();
        game2.setId(UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff01"));
        game2.setStatus(GameStatus.WAITING);
        game2.setMaxPlayers(4);
        game2.setPlayers(List.of(
                new Player(UUID.randomUUID(), 1000),
                new Player(UUID.randomUUID(), 800)
        ));

        when(gameRepository.findAllWithSettings()).thenReturn(List.of(game1, game2));
        when(playerRepository.findAllByGameId(game1.getId())).thenReturn(game1.getPlayers());
        when(playerRepository.findAllByGameId(game2.getId())).thenReturn(game2.getPlayers());


        // Act
        List<GameDto> result = gameService.getGames();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff00"), result.get(0).id());
        assertEquals(UUID.fromString("7fabf988-a888-4dc6-8423-4cd9f620ff01"), result.get(1).id());
    }

    @Test
    void getGamesWithNoGamesReturnsAGameNotFoundException() {
        // Arrange
        given(gameRepository.findAllGamesWithPlayers()).willReturn(List.of());

        // Act & Assert
        GameNotFoundException exception = assertThrows(
                GameNotFoundException.class,
                () -> gameService.getGames()
        );

        assertEquals("No games found", exception.getMessage());
    }

    @Test
    void getByIdWithRounds_ShouldReturnGame_WhenGameExists() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        when(gameRepository.findByIdWithRounds(gameId)).thenReturn(Optional.of(game));

        // Act
        Optional<Game> result = gameService.getByIdWithRounds(gameId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(gameId, result.get().getId());
    }

    @Test
    void getByIdWithRounds_ShouldReturnEmpty_WhenGameDoesNotExist() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findByIdWithRounds(gameId)).thenReturn(Optional.empty());

        // Act
        Optional<Game> result = gameService.getByIdWithRounds(gameId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getMessagesShouldReturnListOfGameMessageDto() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findByIdWithMessages(any(UUID.class))).thenReturn(Optional.of(new Game()));

        // Act
        List<GameMessageDto> result = gameService.getMessages(gameId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getMessagesShouldReturnEmptyList_WhenGameDoesNotExist() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.empty());

        // Act
        List<GameMessageDto> result = gameService.getMessages(gameId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addMessageShouldReturnGameMessageDto() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setEmail("email");
        Player player = new Player();
        player.setAccount(account);

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(game));
        when(accountService.getLoggedInUserEmail()).thenReturn(account.getEmail());
        when(accountRepository.findAccountByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(playerRepository.findByAccountAndGame(account.getId(), gameId)).thenReturn(Optional.of(player));
        when(gameMessageRepository.save(any(GameMessage.class))).thenReturn(new GameMessage());

        // Act
        var result = gameService.addMessage(gameId, "hoi");

        // Assert
        assertNotNull(result);
    }

    @Test
    void addMessageShouldReturnGameNotFoundException_WhenGameDoesNotExist() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setEmail("email");
        Player player = new Player();
        player.setId(UUID.randomUUID());
        player.setAccount(account);

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.empty());
        when(accountService.getLoggedInUserEmail()).thenReturn(account.getEmail());
        when(accountRepository.findAccountByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(playerRepository.findByAccountAndGame(account.getId(), gameId)).thenReturn(Optional.of(player));

        // Act & Assert
        assertThrows(GameNotFoundException.class, () -> gameService.addMessage(gameId, "hoi"));

    }
}
