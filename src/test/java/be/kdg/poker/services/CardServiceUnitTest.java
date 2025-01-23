package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.PlayersHandDto;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Round;
import be.kdg.poker.domain.enums.GameStatus;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.PlayersHandNotFoundException;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CardServiceUnitTest {

    @Autowired
    private CardService cardService;

    @MockBean
    private PlayerRepository playerRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private RoundRepository roundRepository;
    @MockBean
    private HandRankService handRankService;


    @Test
    void getPlayersHandShouldThrowExceptionWhenPlayerNotFound() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findByIdWithHand(playerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PlayersHandNotFoundException.class, () -> cardService.getPlayersHand(playerId));
    }

    @Test
    void getPlayersHandShouldReturnDtoWhenPlayerFound() {
        // Arrange
        UUID playerId = UUID.fromString("fbe9bdbf-4329-4fea-9bed-0449d5677804");
        Player player = new Player();
        player.setId(playerId);
        player.setHand(List.of(new Card(), new Card()));
        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setStatus(GameStatus.IN_PROGRESS);
        Round round = new Round();
        round.setCommunityCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card()));

        when(gameRepository.findGameByPlayerId(playerId)).thenReturn(Optional.of(game));
        when(roundRepository.findLatestByGameIdWithCommunityCards(game.getId())).thenReturn(Optional.of(round));
        when(handRankService.calculateHandScore(anyList())).thenReturn(100);
        when(playerRepository.findByIdWithHand(playerId)).thenReturn(Optional.of(player));

        // Act
        PlayersHandDto result = cardService.getPlayersHand(playerId);

        // Assert
        assertEquals(playerId, result.playerId());
        assertEquals(100, result.score());
    }

    @Test
    void getPlayersHandShouldThrowExceptionWhenGameNotFound() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findByIdWithHand(playerId)).thenReturn(Optional.of(new Player()));
        when(gameRepository.findGameByPlayerId(playerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GameNotFoundException.class, () -> cardService.getPlayersHand(playerId));
    }

    @Test
    void getPlayersHandShouldThrowExceptionWhenRoundNotFound() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        Player player = new Player();
        player.setId(playerId);
        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setStatus(GameStatus.IN_PROGRESS);

        when(gameRepository.findGameByPlayerId(playerId)).thenReturn(Optional.of(game));
        when(roundRepository.findLatestByGameIdWithCommunityCards(game.getId())).thenReturn(Optional.empty());
        when(playerRepository.findByIdWithHand(playerId)).thenReturn(Optional.of(player));

        // Act & Assert
        assertThrows(RoundNotFoundException.class, () -> cardService.getPlayersHand(playerId));
    }
}