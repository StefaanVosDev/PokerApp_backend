package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.enums.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class HandRankServiceUnitTest {
    @Autowired
    private HandRankService handRankService;

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
    void calculateWinnersByHandRanks_ShouldReturnMapOfPlayersAndTemporaryScoresOrderedByPlayerOrder_GivenValidHands() {
        mockPlayers.sort(Comparator.comparing(Player::getId));

        mockPlayers.add(new Player(UUID.randomUUID(), 10));
        mockPlayers.add(new Player(UUID.randomUUID(), 25));
        mockPlayers.add(new Player(UUID.randomUUID(), 100));
        mockPlayers.add(new Player(UUID.randomUUID(), 50));
        mockPlayers.add(new Player(UUID.randomUUID(), 75));

        var mockPlayerHands = new HashMap<Player, List<Card>>();
        var mockHands = new ArrayList<List<Card>>();

        List<Card> mockCommunityCards = new ArrayList<>();
        mockCommunityCards.add(new Card(Suit.HEARTS, 13));
        mockCommunityCards.add(new Card(Suit.HEARTS, 11));
        mockCommunityCards.add(new Card(Suit.HEARTS, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 3));
        mockCommunityCards.add(new Card(Suit.CLUBS, 2));



        mockHands.add(new ArrayList<>(List.of(new Card(Suit.HEARTS, 12), new Card(Suit.HEARTS, 14))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.HEARTS, 12), new Card(Suit.HEARTS, 9))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.HEARTS, 2), new Card(Suit.HEARTS, 3))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 12), new Card(Suit.DIAMONDS, 9))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 11), new Card(Suit.SPADES, 11))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 3), new Card(Suit.DIAMONDS, 10))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 2), new Card(Suit.DIAMONDS, 6))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 4), new Card(Suit.DIAMONDS, 5))));


        for (var mockHand:mockHands) {
            mockHand.addAll(mockCommunityCards);
        }

        for (int i = 0; i < mockHands.size(); i++) {
            var mockPlayer = mockPlayers.get(i);
            var mockHand = mockHands.get(i);
            mockPlayerHands.put(mockPlayer, mockHand);
        }

        var result = handRankService.calculateWinnersByHandRanks(mockPlayers, mockPlayerHands);

        assertEquals(900, result.get(mockPlayers.get(0)).score());
        assertEquals(800, result.get(mockPlayers.get(1)).score());

        for (int i = result.size() - 3; i >= 0; i--) {
            var mockHandRank = result.get(mockPlayers.get(mockPlayers.size() - (i + 1)));
            assertEquals(100 * i, mockHandRank.score());
        }
    }

    @Test
    void calculateWinnersByHandRanks_CaseFakeStraightFlush_ShouldReturnMapOfPlayerAndTemporaryScore_GivenValidHandWithStraightAndFlushButNoStraightFlush() {
        mockPlayers.sort(Comparator.comparing(Player::getId));
        mockPlayers.remove(mockPlayers.get(2));
        mockPlayers.remove(mockPlayers.get(1));

        var mockPlayerHands = new HashMap<Player, List<Card>>();
        var mockHands = new ArrayList<List<Card>>();

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.SPADES, 12));
        mockCommunityCards.add(new Card(Suit.SPADES, 8));
        mockCommunityCards.add(new Card(Suit.SPADES, 7));
        mockCommunityCards.add(new Card(Suit.SPADES, 6));
        mockCommunityCards.add(new Card(Suit.SPADES, 5));

        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 9), new Card(Suit.CLUBS, 13))));

        for (var mockHand:mockHands) {
            mockHand.addAll(mockCommunityCards);
        }

        for (int i = 0; i < mockHands.size(); i++) {
            var mockPlayer = mockPlayers.get(i);
            var mockHand = mockHands.get(i);
            mockPlayerHands.put(mockPlayer, mockHand);
        }

        var result = handRankService.calculateWinnersByHandRanks(mockPlayers, mockPlayerHands);

        var mockHandRank = result.get(mockPlayers.get(0));
        assertEquals(500, mockHandRank.score());
    }

    @Test
    void calculateWinnersByHandRanks_CaseFakeRoyalFlush_ShouldReturnMapOfPlayerAndTemporaryScore_GivenValidHandWithStraightFlushButNoRoyalFlush() {
        mockPlayers.sort(Comparator.comparing(Player::getId));
        mockPlayers.remove(mockPlayers.get(2));
        mockPlayers.remove(mockPlayers.get(1));

        var mockPlayerHands = new HashMap<Player, List<Card>>();
        var mockHands = new ArrayList<List<Card>>();

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.DIAMONDS, 14));
        mockCommunityCards.add(new Card(Suit.SPADES, 13));
        mockCommunityCards.add(new Card(Suit.SPADES, 12));
        mockCommunityCards.add(new Card(Suit.SPADES, 11));
        mockCommunityCards.add(new Card(Suit.SPADES, 10));

        mockHands.add(new ArrayList<>(List.of(new Card(Suit.SPADES, 9), new Card(Suit.CLUBS, 5))));

        for (var mockHand:mockHands) {
            mockHand.addAll(mockCommunityCards);
        }

        for (int i = 0; i < mockHands.size(); i++) {
            var mockPlayer = mockPlayers.get(i);
            var mockHand = mockHands.get(i);
            mockPlayerHands.put(mockPlayer, mockHand);
        }

        var result = handRankService.calculateWinnersByHandRanks(mockPlayers, mockPlayerHands);

        var mockHandRank = result.get(mockPlayers.get(0));
        assertEquals(800, mockHandRank.score());
    }

    @Test
    void calculateWinnersByHandRanks_CaseFullHouseAndFourOfKind_ShouldReturnMapOfPlayersAndTemporaryScoresOrderedByPlayerOrder_GivenValidHands() {
        mockPlayers.sort(Comparator.comparing(Player::getId));
        mockPlayers.remove(mockPlayers.get(2));

        var mockPlayerHands = new HashMap<Player, List<Card>>();
        var mockHands = new ArrayList<List<Card>>();

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.HEARTS, 13));
        mockCommunityCards.add(new Card(Suit.SPADES, 13));
        mockCommunityCards.add(new Card(Suit.HEARTS, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 3));
        mockCommunityCards.add(new Card(Suit.CLUBS, 2));

        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 13), new Card(Suit.CLUBS, 13))));
        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 13), new Card(Suit.DIAMONDS, 10))));

        for (var mockHand:mockHands) {
            mockHand.addAll(mockCommunityCards);
        }

        for (int i = 0; i < mockHands.size(); i++) {
            var mockPlayer = mockPlayers.get(i);
            var mockHand = mockHands.get(i);
            mockPlayerHands.put(mockPlayer, mockHand);
        }

        var result = handRankService.calculateWinnersByHandRanks(mockPlayers, mockPlayerHands);

        var mockHandRank = result.get(mockPlayers.get(0));
        assertEquals(700, mockHandRank.score());

        mockHandRank = result.get(mockPlayers.get(1));
        assertEquals(600, mockHandRank.score());
    }

    @Test
    void calculateWinnersByHandRanks_ShouldReturnSingleEntryMapOfPlayerAndTemporaryScore_GivenOneValidHandAndOneEmptyHand() {
        mockPlayers.sort(Comparator.comparing(Player::getId));
        mockPlayers.remove(mockPlayers.get(2));

        var mockPlayerHands = new HashMap<Player, List<Card>>();
        var mockHands = new ArrayList<List<Card>>();

        var mockCommunityCards = new ArrayList<Card>();
        mockCommunityCards.add(new Card(Suit.HEARTS, 13));
        mockCommunityCards.add(new Card(Suit.SPADES, 13));
        mockCommunityCards.add(new Card(Suit.HEARTS, 10));
        mockCommunityCards.add(new Card(Suit.SPADES, 3));
        mockCommunityCards.add(new Card(Suit.CLUBS, 2));

        mockHands.add(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, 13), new Card(Suit.CLUBS, 13))));
        mockHands.add(new ArrayList<>());

        mockHands.get(0).addAll(mockCommunityCards);

        for (int i = 0; i < mockHands.size(); i++) {
            var mockPlayer = mockPlayers.get(i);
            var mockHand = mockHands.get(i);
            mockPlayerHands.put(mockPlayer, mockHand);
        }

        var result = handRankService.calculateWinnersByHandRanks(mockPlayers, mockPlayerHands);

        assertEquals(1, result.size());
    }

    @Test
    void calculateHandScore_CaseRoyalFlush_ShouldReturn800_GivenValidHand() {
        var mockHand = new ArrayList<Card>();
        mockHand.add(new Card(Suit.HEARTS, 14));
        mockHand.add(new Card(Suit.HEARTS, 13));
        mockHand.add(new Card(Suit.HEARTS, 12));
        mockHand.add(new Card(Suit.HEARTS, 11));
        mockHand.add(new Card(Suit.HEARTS, 10));

        var result = handRankService.calculateHandScore(mockHand);

        assertEquals(900, result);
    }

    @Test
    void calculateHandScore_CaseTwoPair_ShouldReturn200_GivenValidHand() {
        var mockHand = new ArrayList<Card>();
        mockHand.add(new Card(Suit.HEARTS, 14));
        mockHand.add(new Card(Suit.SPADES, 14));
        mockHand.add(new Card(Suit.HEARTS, 12));
        mockHand.add(new Card(Suit.SPADES, 12));

        var result = handRankService.calculateHandScore(mockHand);

        assertEquals(200, result);
    }
}
