package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.RoundDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.Phase;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.domain.enums.Suit;
import be.kdg.poker.exceptions.ResourceNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class RoundServiceUnitTest {

    @Autowired
    private RoundService roundService;

    @MockBean
    private RoundRepository roundRepository;
    @MockBean
    private GameRepository gameRepository;
    @MockBean
    private TurnService turnService;
    @MockBean
    private TurnRepository turnRepository;
    @MockBean
    private GameRoundService gameRoundService;
    @MockBean
    private NotificationService notificationService;



    private Game mockGame;
    private Round mockRound;
    private UUID gameId;
    private UUID roundId;
    private Player mockPlayer1;
    private Player mockPlayer2;
    private Player mockPlayer3;

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

        mockGame.setRounds(new ArrayList<>(List.of(mockRound)));

        mockPlayer1 = new Player();
        mockPlayer1.setId(UUID.randomUUID());
        mockPlayer1.setGame(mockGame);

        mockPlayer2 = new Player();
        mockPlayer2.setId(UUID.randomUUID());
        mockPlayer2.setGame(mockGame);

        mockPlayer3 = new Player();
        mockPlayer3.setId(UUID.randomUUID());
        mockPlayer3.setGame(mockGame);

        mockGame.setPlayers(new ArrayList<>(List.of(mockPlayer1, mockPlayer2, mockPlayer3)));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(mockRound));
    }

    @Test
    void getCommunityCards_ShouldThrowException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roundService.getCommunityCards(gameId));
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void getCommunityCards_ShouldThrowException_WhenRoundNotFound() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithCommunityCards(mockGame)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roundService.getCommunityCards(gameId));
        assertEquals("Round not found", exception.getMessage());
    }

    @Test
    void getCommunityCards_ShouldReturnCommunityCards_WhenPhaseIsFlop() {
        List<Card> communityCards = new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 2),
                new Card(Suit.CLUBS, 5),
                new Card(Suit.DIAMONDS, 10)
        )); // First 3 cards for the flop

        mockRound.setPhase(Phase.FLOP);
        mockRound.setCommunityCards(new ArrayList<>(communityCards));

        mockRound.setCommunityCards(new ArrayList<>(communityCards));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithCommunityCards(mockGame)).thenReturn(Optional.of(mockRound));

        List<Card> result = roundService.getCommunityCards(gameId);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void getCommunityCards_ShouldReturnCommunityCards_WhenPhaseIsTurn() {
        List<Card> communityCards = new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 2),
                new Card(Suit.CLUBS, 5),
                new Card(Suit.DIAMONDS, 10),
                new Card(Suit.SPADES, 14)
        )); // First 4 cards for the turn
        mockRound.setPhase(Phase.TURN);
        mockRound.setCommunityCards(new ArrayList<>(communityCards));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithCommunityCards(mockGame)).thenReturn(Optional.of(mockRound));

        List<Card> result = roundService.getCommunityCards(gameId);

        assertNotNull(result);
        assertEquals(4, result.size());
    }

    @Test
    void getCommunityCards_ShouldReturnCommunityCards_WhenPhaseIsRiver() {
        List<Card> communityCards = new ArrayList<>(List.of(
                new Card(Suit.HEARTS, 10),
                new Card(Suit.CLUBS, 2),
                new Card(Suit.DIAMONDS, 5),
                new Card(Suit.SPADES, 14),
                new Card(Suit.HEARTS, 8)
        )); // First 5 cards for the river
        mockRound.setPhase(Phase.RIVER);
        mockRound.setCommunityCards(new ArrayList<>(communityCards));

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithCommunityCards(mockGame)).thenReturn(Optional.of(mockRound));

        List<Card> result = roundService.getCommunityCards(gameId);

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    void getCommunityCards_ShouldThrowResourceNotFoundException_WhenGameNotFound() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> roundService.getCommunityCards(gameId));
    }

    @Test
    void getCommunityCards_ShouldThrowResourceNotFoundException_WhenRoundNotFound() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByGameWithCommunityCards(mockGame)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> roundService.getCommunityCards(gameId));
    }


    @Test
    void startRound_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roundService.startRound(gameId, roundId));
    }

    @Test
    void startRound_ShouldThrowException_WhenRoundNotFound() {
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roundService.startRound(gameId, roundId));
    }

    @Test
    void startRound_ShouldSetFirstPlayerOnMove() {
        // Arrange
        mockPlayer1.setPosition(0);
        mockPlayer2.setPosition(1);
        mockPlayer3.setPosition(2);

        mockRound.setTurns(new ArrayList<>());

        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.of(mockRound));
        doNothing().when(turnService).addSmallAndBigBlindsToRound(mockRound, mockGame);
        when(turnRepository.save(any(Turn.class))).thenReturn(new Turn());

        // Act
        roundService.startRound(gameId, roundId);

        // Assert
        Turn firstTurn = mockRound.getTurns().get(0);
        assertEquals(mockPlayer1.getId(), firstTurn.getPlayer().getId());
        assertEquals(PlayerStatus.ON_MOVE, firstTurn.getMoveMade());
        assertEquals(Phase.PRE_FLOP, firstTurn.getMadeInPhase());
    }

    @Test
    void createNewRoundIfFinished_ShouldThrowException_WhenRoundNotFound() {
        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roundService.createNewRoundIfFinished(mockGame, roundId));
        assertEquals("Round not found with id: " + roundId, exception.getMessage());
        verify(roundRepository, never()).save(any(Round.class));
    }

    @Test
    void getPlayersLeftInRound_ShouldReturnActivePlayers_WhenSomePlayersFolded() {
        // Arrange
        Turn turn1 = new Turn(mockPlayer1);
        turn1.setMoveMade(PlayerStatus.CHECK);
        Turn turn2 = new Turn(mockPlayer2);
        turn2.setMoveMade(PlayerStatus.CALL);
        Turn turn3 = new Turn(mockPlayer3);
        turn3.setMoveMade(PlayerStatus.FOLD);

        mockRound.setTurns(List.of(turn1, turn2, turn3));

        // Act
        List<Player> activePlayers = roundService.getPlayersLeftInRound(mockRound, mockGame, true);

        // Assert
        assertEquals(2, activePlayers.size());
        assertTrue(activePlayers.contains(mockPlayer1));
        assertTrue(activePlayers.contains(mockPlayer2));
        assertFalse(activePlayers.contains(mockPlayer3));
    }

    @Test
    void getPlayersLeftInRound_ShouldThrowException_WhenRoundIsNull() {
        mockGame.setPlayers(new ArrayList<>());

        assertThrows(NullPointerException.class, () -> roundService.getPlayersLeftInRound(mockRound, mockGame, true));
    }

    @Test
    void getPlayersLeftInRound_ShouldThrowException_WhenGameIsNull() {
        Game game = new Game();
        mockRound.setTurns(new ArrayList<>());

        assertThrows(NullPointerException.class, () -> roundService.getPlayersLeftInRound(mockRound, game, true));
    }

    @Test
    void findById_ShouldReturnRound_WhenRoundExists() {
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(mockRound));

        Round result = roundService.findById(roundId);

        assertNotNull(result);
        assertEquals(mockRound, result);
        verify(roundRepository, times(1)).findById(roundId);
    }

    @Test
    void findById_ShouldThrowException_WhenRoundNotFound() {
        when(roundRepository.findById(roundId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roundService.findById(roundId));
        assertEquals("Round not found", exception.getMessage());
        verify(roundRepository, times(1)).findById(roundId);
    }

    @Test
    void getLatestByGameWithTurns_ShouldReturnRound_WhenRoundExists() {
        when(roundRepository.findLatestByGameWithTurns(mockGame.getId())).thenReturn(Optional.of(mockRound));

        Optional<Round> result = roundService.getLatestByGameWithTurns(mockGame.getId());

        assertTrue(result.isPresent());
        assertEquals(mockRound, result.get());
        verify(roundRepository, times(1)).findLatestByGameWithTurns(mockGame.getId());
    }

    @Test
    void getLatestByGameWithTurns_ShouldReturnEmpty_WhenNoRoundExists() {
        when(roundRepository.findLatestByGameWithTurns(mockGame.getId())).thenReturn(Optional.empty());

        Optional<Round> result = roundService.getLatestByGameWithTurns(mockGame.getId());

        assertFalse(result.isPresent());
        verify(roundRepository, times(1)).findLatestByGameWithTurns(mockGame.getId());
    }

    @Test
    void addCommunityCard_ShouldAddCardToCommunityCards_WhenDeckIsNotEmpty() {
        mockRound.setCommunityCards(new ArrayList<>());

        Card firstCard = new Card(Suit.HEARTS, 10);
        List<Card> mockDeck = new ArrayList<>(List.of(firstCard, new Card(Suit.CLUBS, 2)));

        when(roundRepository.findDeckByRoundId(roundId)).thenReturn(mockDeck);

        roundService.addCommunityCard(mockRound);

        assertEquals(1, mockRound.getCommunityCards().size());
        assertEquals(1, mockDeck.size()); // Deck should now have one less card
        verify(roundRepository, times(1)).save(mockRound);
    }

    @Test
    void addCommunityCard_ShouldThrowException_WhenDeckIsEmpty() {
        mockRound.setCommunityCards(new ArrayList<>());

        List<Card> emptyDeck = new ArrayList<>();
        when(roundRepository.findDeckByRoundId(roundId)).thenReturn(emptyDeck);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> roundService.addCommunityCard(mockRound));
        assertEquals("Deck is empty, cannot add community card", exception.getMessage());
        verify(roundRepository, never()).save(any());
    }

    @Test
    void changeRoundPhase_ShouldChangeToFlop_WhenCurrentPhaseIsPreFlop() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        // Act
        roundService.changeRoundPhase(mockRound);

        // Assert
        assertEquals(Phase.FLOP, mockRound.getPhase());
    }

    @Test
    void changeRoundPhase_ShouldChangeToTurn_WhenCurrentPhaseIsFlop() {
        // Arrange
        mockRound.setPhase(Phase.FLOP);

        // Act
        roundService.changeRoundPhase(mockRound);

        // Assert
        assertEquals(Phase.TURN, mockRound.getPhase());
    }

    @Test
    void changeRoundPhase_ShouldChangeToRiver_WhenCurrentPhaseIsTurn() {
        // Arrange
        mockRound.setPhase(Phase.TURN);

        // Act
        roundService.changeRoundPhase(mockRound);

        // Assert
        assertEquals(Phase.RIVER, mockRound.getPhase());
    }

    @Test
    void changeRoundPhase_ShouldChangeToFinished_WhenCurrentPhaseIsRiver() {
        // Arrange
        mockRound.setPhase(Phase.RIVER);

        // Act
        roundService.changeRoundPhase(mockRound);

        // Assert
        assertEquals(Phase.FINISHED, mockRound.getPhase());
    }

    @Test
    void addCommunityCardsBasedOnPhase_ShouldAddThreeCards_WhenPhaseIsFlop() {
        // Arrange
        mockRound.setPhase(Phase.FLOP);
        when(roundRepository.findDeckByRoundId(mockRound.getId())).thenReturn(mockRound.getDeck());

        // Act
        roundService.addCommunityCardsBasedOnPhase(mockRound);

        // Assert
        assertEquals(3, mockRound.getCommunityCards().size());
        verify(roundRepository, times(3)).save(mockRound);
    }

    @Test
    void addCommunityCardsBasedOnPhase_ShouldAddOneCard_WhenPhaseIsTurn() {
        // Arrange
        mockRound.setPhase(Phase.TURN);
        when(roundRepository.findDeckByRoundId(mockRound.getId())).thenReturn(mockRound.getDeck());

        // Act
        roundService.addCommunityCardsBasedOnPhase(mockRound);

        // Assert
        assertEquals(1, mockRound.getCommunityCards().size());
        verify(roundRepository, times(1)).save(mockRound);
    }

    @Test
    void addCommunityCardsBasedOnPhase_ShouldAddOneCard_WhenPhaseIsRiver() {
        // Arrange
        mockRound.setPhase(Phase.RIVER);
        when(roundRepository.findDeckByRoundId(mockRound.getId())).thenReturn(mockRound.getDeck());

        // Act
        roundService.addCommunityCardsBasedOnPhase(mockRound);

        // Assert
        assertEquals(1, mockRound.getCommunityCards().size());
        verify(roundRepository, times(1)).save(mockRound);
    }

    @Test
    void changePhase_ShouldFinishRound_WhenOnlyOnePlayerLeft() {
        // Arrange
        mockRound.setTurns(new ArrayList<>());
        mockGame.setPlayers(List.of(mockPlayer1));

        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        doNothing().when(turnService).addSmallAndBigBlindsToRound(mockRound, mockGame);
        when(turnService.create(any(Round.class), any(Game.class))).thenReturn(new Turn());
        when(roundRepository.save(any(Round.class))).thenReturn(new Round());
        when(gameRoundService.create(any(Game.class))).thenReturn(new Round());

        // Act
        roundService.changePhase(gameId, roundId);

        // Assert
        assertEquals(Phase.FINISHED, mockRound.getPhase());
        verify(roundRepository, times(1)).save(mockRound);
    }

    @Test
    void changePhase_ShouldChangePhaseAndAddCommunityCards_WhenAllPlayersMovedAndBetsMatched() {
        // Arrange
        mockRound.setTurns(new ArrayList<>(List.of(
                new Turn(mockPlayer1, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP),
                new Turn(mockPlayer2, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP),
                new Turn(mockPlayer3, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP)
        )));

        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        doNothing().when(turnService).addSmallAndBigBlindsToRound(mockRound, mockGame);
        when(turnService.create(any(Round.class), any(Game.class))).thenReturn(new Turn());
        when(turnRepository.save(any(Turn.class))).thenReturn(new Turn());
        when(roundRepository.findDeckByRoundId(any(UUID.class))).thenReturn(mockRound.getDeck());
        doNothing().when(notificationService).notifyPlayerOnMove(any(Player.class), any(Game.class));

        // Act
        roundService.changePhase(gameId, roundId);

        // Assert
        assertEquals(Phase.FLOP, mockRound.getPhase());
        assertEquals(3, mockRound.getCommunityCards().size());
    }

    @Test
    void changePhase_ShouldSetNextPlayerTurn_WhenNotAllPlayersMovedOrBetsNotMatched() {
        // Arrange
        mockRound.setTurns(new ArrayList<>(List.of(new Turn(mockPlayer1, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP))));

        when(roundRepository.findByIdWithTurns(roundId)).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByIdWithPlayers(gameId)).thenReturn(Optional.of(mockGame));
        doNothing().when(turnService).addSmallAndBigBlindsToRound(mockRound, mockGame);
        when(turnService.create(any(Round.class), any(Game.class))).thenReturn(new Turn());
        when(roundRepository.findDeckByRoundId(any(UUID.class))).thenReturn(mockRound.getDeck());
        doNothing().when(notificationService).notifyPlayerOnMove(any(Player.class), any(Game.class));

        // Act
        roundService.changePhase(gameId, roundId);

        // Assert
        assertEquals(Phase.PRE_FLOP, mockRound.getPhase());
        assertEquals(2, mockRound.getTurns().size());
        assertEquals(0, mockRound.getCommunityCards().size());
    }

    @Test
    void validatePlayersHaveMatchedBets_ShouldReturnFalse_WhenPlayerHasNotMatchedBet() {
        // Arrange
        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        turn1.setMoneyGambled(10);

        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        turn2.setMoneyGambled(20);

        mockRound.setTurns(List.of(turn1, turn2));

        when(roundRepository.findByIdWithTurns(mockRound.getId())).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByIdWithPlayers(mockGame.getId())).thenReturn(Optional.of(mockGame));

        // Act
        boolean result = roundService.validatePlayersHaveMatchedBets(mockGame, mockRound);

        // Assert
        assertFalse(result);
    }

    @Test
    void getCurrentRound_ShouldReturnRound_WhenRoundExists() {
        // Arrange
        when(roundRepository.findLatestByGameWithDeck(gameId)).thenReturn(Optional.of(mockRound));

        // Act
        Optional<Round> result = roundService.getCurrentRound(gameId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockRound, result.get());
        verify(roundRepository, times(1)).findLatestByGameWithDeck(gameId);
    }

    @Test
    void getCurrentRound_ShouldReturnEmpty_WhenNoRoundExists() {
        // Arrange
        when(roundRepository.findLatestByGameWithDeck(gameId)).thenReturn(Optional.empty());

        // Act
        Optional<Round> result = roundService.getCurrentRound(gameId);

        // Assert
        assertFalse(result.isPresent());
        verify(roundRepository, times(1)).findLatestByGameWithDeck(gameId);
    }

    @Test
    void mapToDto_ShouldReturnRoundDto_WithCorrectValues() {
        // Arrange
        int dealerIndex = 1;
        Phase phase = Phase.PRE_FLOP;
        mockRound.setPhase(phase);
        mockRound.setDealerIndex(dealerIndex);

        // Act
        RoundDto roundDto = roundService.mapToDto(mockRound);

        // Assert
        assertEquals(roundId, roundDto.id());
        assertEquals(phase, roundDto.phase());
        assertEquals(dealerIndex, roundDto.dealerIndex());
        assertNull(roundDto.communityCards());
        assertNull(roundDto.turns());
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnTrue_WhenAllPlayersMadeMoves() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1, turn2, turn3));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnFalse_WhenNotAllPlayersMadeMoves() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnTrue_WhenAllPlayersMadeMovesOnTurn() {
        // Arrange
        mockRound.setPhase(Phase.TURN);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.CALL, mockRound, Phase.TURN);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.FOLD, mockRound, Phase.TURN);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CALL, mockRound, Phase.TURN);
        mockRound.setTurns(List.of(turn1, turn2, turn3));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnTrue_WhenSomePlayersFoldedAndOthersMadeMoves() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.FOLD, mockRound, Phase.PRE_FLOP);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1, turn2, turn3));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnFalse_WhenPreFlopWithBigBlindMoveIncluded() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.SMALL_BLIND, mockRound, Phase.PRE_FLOP);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.BIG_BLIND, mockRound, Phase.PRE_FLOP);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1, turn2, turn3));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnTrue_WhenPreFlopWithBigBlindMoveMadeDifferentMoves() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.SMALL_BLIND, mockRound, Phase.PRE_FLOP);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.BIG_BLIND, mockRound, Phase.PRE_FLOP);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn4 = new Turn(mockPlayer1, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn5 = new Turn(mockPlayer2, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1, turn2, turn3, turn4, turn5));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateAllPlayersHaveMadeMoves_ShouldReturnTrue_WhenPreActiveMoveHappened() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);

        Turn turn1 = new Turn(mockPlayer1, PlayerStatus.CHECK, mockRound, Phase.PRE_FLOP);
        Turn turn2 = new Turn(mockPlayer2, PlayerStatus.RAISE, mockRound, Phase.PRE_FLOP);
        Turn turn3 = new Turn(mockPlayer3, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        Turn turn4 = new Turn(mockPlayer1, PlayerStatus.CALL, mockRound, Phase.PRE_FLOP);
        mockRound.setTurns(List.of(turn1, turn2, turn3, turn4));

        // Act
        boolean result = roundService.validateAllPlayersHaveMadeMoves(mockGame, mockRound);

        // Assert
        assertTrue(result);
    }

    @Test
    void runThroughLastRound_ShouldChangePhaseToFinished_WhenPhaseIsPreFlop() {
        // Arrange
        mockRound.setPhase(Phase.PRE_FLOP);
        when(roundRepository.findDeckByRoundId(mockRound.getId())).thenReturn(mockRound.getDeck());

        // Act
        roundService.runThroughLastRound(gameId, roundId, mockRound);

        // Assert
        assertEquals(Phase.FINISHED, mockRound.getPhase());
        assertEquals(5, mockRound.getCommunityCards().size());
    }

    @Test
    void runThroughLastRound_ShouldChangePhaseToFinished_WhenPhaseIsTurn() {
        // Arrange
        mockRound.setPhase(Phase.TURN);
        when(roundRepository.findDeckByRoundId(mockRound.getId())).thenReturn(mockRound.getDeck());

        // Act
        roundService.runThroughLastRound(gameId, roundId, mockRound);

        // Assert
        assertEquals(Phase.FINISHED, mockRound.getPhase());
        assertEquals(1, mockRound.getCommunityCards().size());
    }
}