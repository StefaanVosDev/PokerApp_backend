package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.AllInDto;
import be.kdg.poker.controllers.dto.CalculateRoundWinnerDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.domain.enums.Suit;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.InvalidWinnerException;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DividePotServiceUnitTest {
    @Autowired
    private DividePotService dividePotService;

    @MockBean
    private RoundRepository roundRepository;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private TurnRepository turnRepository;

    @MockBean
    private PlayerRepository playerRepository;

    @MockBean
    private RoundService roundService;
    private List<Player> mockPlayers;

    @BeforeEach
    void setUp() {
        mockPlayers = new ArrayList<>();

        var mockPlayer1 = new Player();
        mockPlayer1.setId(UUID.fromString("beb10f7f-896f-4d3a-8ce5-2cd54863a514"));
        mockPlayer1.setMoney(10);
        mockPlayers.add(mockPlayer1);

        var mockPlayer2 = new Player();
        mockPlayer2.setId(UUID.fromString("c62ccf90-bd3a-4376-8e9c-3fa54ceaf6ba"));
        mockPlayer2.setMoney(25);
        mockPlayers.add(mockPlayer2);

        var mockPlayer3 = new Player();
        mockPlayer3.setId(UUID.fromString("d1c9b4df-2092-41d7-b898-1c4424c98613"));
        mockPlayer3.setMoney(100);
        mockPlayers.add(mockPlayer3);
    }

    @Test
    void getCalculateRoundWinnerDto_ShouldReturnValidDtoGivenValidRoundId() {
        //ARRANGE
        var roundId = UUID.randomUUID();

        var mockHand1 = new ArrayList<Card>();
        var mockHand2 = new ArrayList<Card>();
        var mockHand3 = new ArrayList<Card>();

        mockHand1.add(new Card(Suit.SPADES, 2));
        mockHand1.add(new Card(Suit.SPADES, 3));
        var mockPlayer1 = mockPlayers.get(0);
        mockPlayer1.setHand(mockHand1);
        mockPlayers.set(0, mockPlayer1);

        mockHand2.add(new Card(Suit.SPADES, 4));
        mockHand2.add(new Card(Suit.SPADES, 5));
        var mockPlayer2 = mockPlayers.get(1);
        mockPlayer2.setHand(mockHand2);
        mockPlayers.set(1, mockPlayer2);

        mockHand3.add(new Card(Suit.SPADES, 6));
        mockHand3.add(new Card(Suit.SPADES, 7));
        var mockPlayer3 = mockPlayers.get(2);
        mockPlayer3.setHand(mockHand3);
        mockPlayers.set(2, mockPlayer3);

        var mockRound = new Round();
        mockRound.setId(roundId);

        var mockCommunityCards = new ArrayList<Card>();

        mockCommunityCards.add(new Card(Suit.SPADES, 8));
        mockCommunityCards.add(new Card(Suit.SPADES, 9));
        mockCommunityCards.add(new Card(Suit.SPADES, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 11));
        mockCommunityCards.add(new Card(Suit.SPADES, 12));

        mockRound.setCommunityCards(mockCommunityCards);

        var mockGame = new Game();
        mockGame.setId(UUID.randomUUID());

        var mockTurns = new ArrayList<Turn>();
        var mockTurn1 = new Turn();
        var mockTurn2 = new Turn();
        var mockTurn3 = new Turn();
        mockTurn1.setPlayer(mockPlayer1);
        mockTurn2.setPlayer(mockPlayer2);
        mockTurn3.setPlayer(mockPlayer3);
        mockTurn1.setMoneyGambled(10);
        mockTurn2.setMoneyGambled(25);
        mockTurn3.setMoneyGambled(25);
        mockTurn1.setMoveMade(PlayerStatus.ALL_IN);
        mockTurn2.setMoveMade(PlayerStatus.ALL_IN);
        mockTurn3.setMoveMade(PlayerStatus.CALL);

        mockTurns.add(mockTurn1);
        mockTurns.add(mockTurn2);
        mockTurns.add(mockTurn3);

        List<Player> mockPlayersLeftInGame = new ArrayList<>();
        mockPlayersLeftInGame.add(mockPlayer1);
        mockPlayersLeftInGame.add(mockPlayer2);
        mockPlayersLeftInGame.add(mockPlayer3);


        when(playerRepository.findByGameIdWithHands(any(UUID.class))).thenReturn(mockPlayers);
        when(roundRepository.findByIdWithCommunityCards(any(UUID.class))).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByRoundId(any(UUID.class))).thenReturn(Optional.of(mockGame));
        when(turnRepository.findByRoundIdWithPlayer(any(UUID.class))).thenReturn(mockTurns);
        when(roundService.getPlayersLeftInRound(mockRound, mockGame, false)).thenReturn(mockPlayersLeftInGame);
        when(playerRepository.findByIdWithHand(mockPlayer1.getId())).thenReturn(Optional.of(mockPlayer1));
        when(playerRepository.findByIdWithHand(mockPlayer2.getId())).thenReturn(Optional.of(mockPlayer2));
        when(playerRepository.findByIdWithHand(mockPlayer3.getId())).thenReturn(Optional.of(mockPlayer3));

        //ACT
        var optResult = dividePotService.getCalculateRoundWinnerDto(roundId);


        //ASSERT
        assertTrue(optResult.isPresent());
        var result = optResult.get();

        var resultPlayerHands = result.hands();
        resultPlayerHands = resultPlayerHands.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Player, List<Card>> entry) -> entry.getKey().getId().toString()).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                        ));

        var handPlayers = resultPlayerHands.keySet().stream().toList();
        var resultPlayers = result.players();

        assertEquals(handPlayers.size(), resultPlayers.size());
        assertTrue(handPlayers.containsAll(resultPlayers));
        assertEquals(resultPlayers.size(), mockPlayersLeftInGame.size());
        assertTrue(resultPlayers.containsAll(mockPlayersLeftInGame));

        var resultHands = result.hands().values().stream().toList();
        resultHands.forEach(hand -> {
            assertEquals(7, hand.size());
            assertTrue(hand.containsAll(mockCommunityCards));
        });
        assertTrue(resultHands.get(2).containsAll(mockHand1));
        assertTrue(resultHands.get(1).containsAll(mockHand2));
        assertTrue(resultHands.get(0).containsAll(mockHand3));

        var stakesPlayers = new ArrayList<Player>();
        var allInDtoList = result.stakes().keySet().stream().toList();
        allInDtoList.forEach(dto -> stakesPlayers.add(dto.player()));
        assertEquals(stakesPlayers.size(), resultPlayers.size());
        assertTrue(stakesPlayers.containsAll(resultPlayers));

        var stakesValues = result.stakes().values().stream().toList();
        assertEquals(3, stakesValues.size());
        var expectedStakes = new ArrayList<Integer>();
        expectedStakes.add(10);
        expectedStakes.add(25);
        expectedStakes.add(25);
        assertTrue(stakesValues.containsAll(expectedStakes));
    }

    @Test
    void calculateWinners_ShouldReturnClearListOfWinnersWithEachPlacementOnlyHavingOneMember_GivenClearDifferenceInHandRank() {
        // ARRANGE
        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(25);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }

        var mockHands = new ArrayList<List<Card>>();

        var mockHand1 = new ArrayList<Card>();
        mockHand1.add(new Card(Suit.DIAMONDS, 9));
        mockHand1.add(new Card(Suit.CLUBS, 9));

        var mockHand2 = new ArrayList<Card>();
        mockHand2.add(new Card(Suit.DIAMONDS, 10));
        mockHand2.add(new Card(Suit.CLUBS, 3));

        var mockHand3 = new ArrayList<Card>();
        mockHand3.add(new Card(Suit.HEARTS, 12));
        mockHand3.add(new Card(Suit.HEARTS, 9));

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.HEARTS, 13));
        mockCommunityCards.add(new Card(Suit.HEARTS, 11));
        mockCommunityCards.add(new Card(Suit.HEARTS, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 3));
        mockCommunityCards.add(new Card(Suit.CLUBS, 2));

        mockHand1.addAll(mockCommunityCards);
        mockHand2.addAll(mockCommunityCards);
        mockHand3.addAll(mockCommunityCards);

        mockHands.add(mockHand1);
        mockHands.add(mockHand2);
        mockHands.add(mockHand3);

        var mockPlayerHands = new HashMap<Player, List<Card>>();

        var mockGame = new Game();
        UUID gameId = mockGame.getId();

        for (int i = 0; i < mockHands.size(); i++) {
            var player = mockPlayers.get(i);
            var hand = mockHands.get(i);
            mockPlayerHands.put(player, hand);
        }

        var mockSituation = new CalculateRoundWinnerDto(mockPlayers, mockPlayerHands, mockStakes);

        // ACT
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(gameRepository.findGameByPlayerId(mockPlayers.get(0).getId())).thenReturn(Optional.of(mockGame)); // Mock this call
        var result = dividePotService.calculateWinners(mockSituation);

        // ASSERT
        assertEquals(3, result.size());
        var winners = result.get(0);
        assertEquals(1, winners.size());
        var winner = winners.get(0);
        assertEquals(mockPlayers.get(2), winner);
    }


    @Test
    void calculateWinners_ShouldReturnClearListOfWinnersWithEachPlacementOnlyHavingOneMember_GivenDifferenceInHandRankVariation() {
        //ARRANGE
        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(25);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }

        var mockHands = new ArrayList<List<Card>>();

        var mockHand1 = new ArrayList<Card>();
        mockHand1.add(new Card(Suit.CLUBS, 13));
        mockHand1.add(new Card(Suit.CLUBS, 10));

        var mockHand2 = new ArrayList<Card>();
        mockHand2.add(new Card(Suit.DIAMONDS, 3));
        mockHand2.add(new Card(Suit.SPADES, 3));

        var mockHand3 = new ArrayList<Card>();
        mockHand3.add(new Card(Suit.HEARTS, 8));
        mockHand3.add(new Card(Suit.HEARTS, 4));

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.HEARTS, 13));
        mockCommunityCards.add(new Card(Suit.SPADES, 13));
        mockCommunityCards.add(new Card(Suit.HEARTS, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 3));
        mockCommunityCards.add(new Card(Suit.CLUBS, 2));

        mockHand1.addAll(mockCommunityCards);
        mockHand2.addAll(mockCommunityCards);
        mockHand3.addAll(mockCommunityCards);

        mockHands.add(mockHand1);
        mockHands.add(mockHand2);
        mockHands.add(mockHand3);

        var mockPlayerHands = new HashMap<Player, List<Card>>();


        UUID gameId = UUID.randomUUID();
        var mockGame = new Game();

        for (int i = 0; i < mockHands.size(); i++) {
            var player = mockPlayers.get(i);
            var hand = mockHands.get(i);
            mockPlayerHands.put(player, hand);
        }

        var mockSituation = new CalculateRoundWinnerDto(mockPlayers, mockPlayerHands, mockStakes);
        var mockRound = new Round();
        mockRound.setId(UUID.randomUUID());

        //ACT
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(gameRepository.findGameByPlayerId(mockPlayers.get(0).getId())).thenReturn(Optional.of(mockGame)); // Mock this call
        var result = dividePotService.calculateWinners(mockSituation);

        //ASSERT
        assertEquals(3, result.size());
        var winners = result.get(0);
        assertEquals(1, winners.size());
        var winner = winners.get(0);
        assertEquals(winner, mockPlayers.get(0));
    }

    @Test
    void dividePot_ShouldReturnMapOfPlayersWithTheirEarningsOfWhichOnlyOneShouldBeDifferentFromZeroAndUpdateMoneyForWinner_GivenSortedGroupsOfPlayersAndValidStakes() {
        //ARRANGE
        var mockPlayersByWinIndex = new ArrayList<List<Player>>();
        for (int i = mockPlayers.size() - 1; i > 0; i--) {
            var mockPlayer = mockPlayers.get(i);
            var playersByWinIndexEntry = new ArrayList<>(new ArrayList<Player>());
            playersByWinIndexEntry.add(mockPlayer);
            mockPlayersByWinIndex.add(playersByWinIndexEntry);
        }

        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(25);
        mockStakesValues.add(25);
        mockStakesValues.add(10);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }
        var mockSituation = new CalculateRoundWinnerDto(null, null, mockStakes);

        when(playerRepository.save(mockPlayers.get(0))).thenReturn(mockPlayers.get(0));
        when(playerRepository.save(mockPlayers.get(1))).thenReturn(mockPlayers.get(1));
        when(playerRepository.save(mockPlayers.get(2))).thenReturn(mockPlayers.get(2));


        //ACT
        Map<Player, Integer> result = null;
        try {
            result = dividePotService.dividePot(mockPlayersByWinIndex, mockSituation);
        } catch (InvalidWinnerException e) {
            fail(e.getMessage());
        }


        //ASSERT
        verify(playerRepository, times(1)).save(any(Player.class));
        assertEquals(60, result.get(mockPlayers.get(2)));

        var losers = new ArrayList<>(result.keySet().stream().toList());
        losers.remove(mockPlayers.get(2));
        for (var loser:losers) {
            assertEquals(0, result.get(loser));
        }
    }

    @Test
    void dividePot_ShouldReturnMapOfPlayersWithTheirEarningsOfWhichOnlyFirstAndSecondPlaceShouldBeDifferentFromZeroAndUpdateMoneyForWinners_GivenSortedGroupsOfPlayersAndValidStakes() {
        //ARRANGE
        var mockPlayersByWinIndex = new ArrayList<List<Player>>();
        for (var player: mockPlayers) {
            var playersByWinIndexEntry = new ArrayList<>(new ArrayList<Player>());
            playersByWinIndexEntry.add(player);
            mockPlayersByWinIndex.add(playersByWinIndexEntry);
        }

        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(30);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }
        var mockSituation = new CalculateRoundWinnerDto(null, null, mockStakes);

        when(playerRepository.save(mockPlayers.get(0))).thenReturn(mockPlayers.get(0));
        when(playerRepository.save(mockPlayers.get(1))).thenReturn(mockPlayers.get(1));
        when(playerRepository.save(mockPlayers.get(2))).thenReturn(mockPlayers.get(2));


        //ACT
        Map<Player, Integer> result = null;
        try {
            result = dividePotService.dividePot(mockPlayersByWinIndex, mockSituation);
        } catch (InvalidWinnerException e) {
            fail(e.getMessage());
        }


        //ASSERT
        verify(playerRepository, times(2)).save(any(Player.class));
        assertEquals(30, result.get(mockPlayers.get(0)));
        assertEquals(35, result.get(mockPlayers.get(1)));
    }

    @Test
    void dividePot_ShouldReturnMapOfPlayersWithTheirEarningsOfWhichAllShouldBeDifferentFromZeroAndUpdateMoney_GivenSortedGroupsOfPlayersAndValidStakes() {
        //ARRANGE
        var mockPlayersByWinIndex = new ArrayList<List<Player>>();
        for (var player: mockPlayers) {
            var playersByWinIndexEntry = new ArrayList<>(new ArrayList<Player>());
            playersByWinIndexEntry.add(player);
            mockPlayersByWinIndex.add(playersByWinIndexEntry);
        }

        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(60);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }
        var mockSituation = new CalculateRoundWinnerDto(null, null, mockStakes);

        when(playerRepository.save(mockPlayers.get(0))).thenReturn(mockPlayers.get(0));
        when(playerRepository.save(mockPlayers.get(1))).thenReturn(mockPlayers.get(1));
        when(playerRepository.save(mockPlayers.get(2))).thenReturn(mockPlayers.get(2));


        //ACT
        Map<Player, Integer> result = null;
        try {
            result = dividePotService.dividePot(mockPlayersByWinIndex, mockSituation);
        } catch (InvalidWinnerException e) {
            fail(e.getMessage());
        }


        //ASSERT
        verify(playerRepository, times(3)).save(any(Player.class));
        assertEquals(30, result.get(mockPlayers.get(0)));
        assertEquals(60, result.get(mockPlayers.get(1)));
        assertEquals(5, result.get(mockPlayers.get(2)));
    }

    @Test
    void dividePot_ShouldReturnMapOfPlayersWithTheirEarningsOfWhichTwoShouldBeDifferentFromZeroAndEqualToEachOtherAndUpdateMoneyForWinners_GivenSortedGroupsOfPlayersAndValidStakes() {
        //ARRANGE
        var mockPlayersByWinIndex = new ArrayList<List<Player>>();
        var winners = new ArrayList<Player>();
        winners.add(mockPlayers.get(0));
        winners.add(mockPlayers.get(1));
        mockPlayersByWinIndex.add(winners);
        for (var player: mockPlayers.stream().filter(p -> !winners.contains(p)).toList()) {
            var playersByWinIndexEntry = new ArrayList<>(new ArrayList<Player>());
            playersByWinIndexEntry.add(player);
            mockPlayersByWinIndex.add(playersByWinIndexEntry);
        }


        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(25);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }
        var mockSituation = new CalculateRoundWinnerDto(null, null, mockStakes);

        when(playerRepository.save(mockPlayers.get(0))).thenReturn(mockPlayers.get(0));
        when(playerRepository.save(mockPlayers.get(1))).thenReturn(mockPlayers.get(1));
        when(playerRepository.save(mockPlayers.get(2))).thenReturn(mockPlayers.get(2));


        //ACT
        Map<Player, Integer> result = null;
        try {
            result = dividePotService.dividePot(mockPlayersByWinIndex, mockSituation);
        } catch (InvalidWinnerException e) {
            fail(e.getMessage());
        }


        //ASSERT
        verify(playerRepository, times(2)).save(any(Player.class));
        assertEquals(30, result.get(mockPlayers.get(0)));
        assertEquals(30, result.get(mockPlayers.get(1)));

        var losers = new ArrayList<>(result.keySet().stream().toList());
        losers.remove(mockPlayers.get(0));
        losers.remove(mockPlayers.get(1));
        for (var loser : losers) {
            assertEquals(0, result.get(loser));
        }
    }

    @Test
    void dividePot_ShouldReturnMapOfPlayersWithTheirZeroEarnings_GivenInvalidSortedGroupsOfPlayersAndValidStakes() {
        //ARRANGE
        var mockPlayersByWinIndex = new ArrayList<List<Player>>();
        var winners = new ArrayList<Player>();
        mockPlayersByWinIndex.add(winners);
        for (var player: mockPlayers) {
            var playersByWinIndexEntry = new ArrayList<>(new ArrayList<Player>());
            playersByWinIndexEntry.add(player);
            mockPlayersByWinIndex.add(playersByWinIndexEntry);
        }

        var mockStakesValues = new ArrayList<Integer>();
        mockStakesValues.add(10);
        mockStakesValues.add(25);
        mockStakesValues.add(25);
        var mockStakesKeys = new ArrayList<AllInDto>();
        mockStakesKeys.add(new AllInDto(mockPlayers.get(0), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(1), true));
        mockStakesKeys.add(new AllInDto(mockPlayers.get(2), false));
        var mockStakes = new HashMap<AllInDto, Integer>();
        for (int i = 0; i < mockStakesKeys.size(); i++) {
            var key = mockStakesKeys.get(i);
            var value = mockStakesValues.get(i);
            mockStakes.put(key, value);
        }
        var mockSituation = new CalculateRoundWinnerDto(null, null, mockStakes);

        //ACT
        Executable executable = () -> dividePotService.dividePot(mockPlayersByWinIndex, mockSituation);

        //ASSERT
        verify(playerRepository, times(0)).save(any(Player.class));
        assertThrows(InvalidWinnerException.class, executable);
    }

    @Test
    void getCalculateRoundWinnerDto_ShouldThrowGameNotFoundExceptionGivenInvalidLinkedRoundId() {
        //ARRANGE
        var roundId = UUID.randomUUID();

        var mockRound = new Round();
        mockRound.setId(roundId);

        var mockCommunityCards = new ArrayList<Card>();

        mockCommunityCards.add(new Card(Suit.SPADES, 8));
        mockCommunityCards.add(new Card(Suit.SPADES, 9));
        mockCommunityCards.add(new Card(Suit.SPADES, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 11));
        mockCommunityCards.add(new Card(Suit.SPADES, 12));

        mockRound.setCommunityCards(mockCommunityCards);


        when(roundRepository.findByIdWithCommunityCards(any(UUID.class))).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByRoundId(any(UUID.class))).thenReturn(Optional.empty());

        //ACT
        Executable executable = () -> dividePotService.getCalculateRoundWinnerDto(roundId);


        //ASSERT
        assertThrows(GameNotFoundException.class, executable);
        try {
            executable.execute();
        } catch (Throwable e) {
            assertEquals("round with id " + roundId + " does not have a game", e.getMessage());
        }
    }

    @Test
    void getCalculateRoundWinnerDto_ShouldThrowTurnNotFoundExceptionGivenEmptyRound() {
        //ARRANGE
        var roundId = UUID.randomUUID();

        var mockRound = new Round();
        mockRound.setId(roundId);

        var mockCommunityCards = new ArrayList<Card>();

        mockCommunityCards.add(new Card(Suit.SPADES, 8));
        mockCommunityCards.add(new Card(Suit.SPADES, 9));
        mockCommunityCards.add(new Card(Suit.SPADES, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 11));
        mockCommunityCards.add(new Card(Suit.SPADES, 12));

        mockRound.setCommunityCards(mockCommunityCards);

        var mockPlayer4 = new Player();
        mockPlayer4.setId(UUID.fromString("e16f56e1-6531-4e03-ab13-c10a053ce3b7"));
        mockPlayers.add(mockPlayer4);

        when(roundRepository.findByIdWithCommunityCards(any(UUID.class))).thenReturn(Optional.of(mockRound));
        when(gameRepository.findByRoundId(any(UUID.class))).thenReturn(Optional.of(new Game()));
        when(turnRepository.findByRoundIdWithPlayer(any(UUID.class))).thenReturn(new ArrayList<>());

        //ACT and ASSERT
        assertThrows(TurnNotFoundException.class, () -> dividePotService.getCalculateRoundWinnerDto(roundId));
    }
}