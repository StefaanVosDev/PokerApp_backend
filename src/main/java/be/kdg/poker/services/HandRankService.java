package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.CardPropsCountsDto;
import be.kdg.poker.controllers.dto.HandRankDto;
import be.kdg.poker.controllers.dto.ProcessDuoComboDto;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.enums.Suit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HandRankService {

    public Map<Player, HandRankDto> calculateWinnersByHandRanks(List<Player> players, Map<Player, List<Card>> hands) {
        log.info("successfully started hand rank analysis");
        Map<Player, HandRankDto> playersWithRank = new HashMap<>();
        for (int i = 0; i < hands.size(); i++) {
            var player = players.get(i);
            var hand = hands.get(player);

            hand.sort(Comparator.comparing(Card::getRank).reversed());

            CardPropsCountsDto countings = setupCardPropsCountsDto(hand);

            var ranks = hand.stream().map(Card::getRank).distinct().sorted().toList();

            log.info("determining number of four and three of a kinds and pairs");
            var cardWithSameRankCombinationCount = countCardsWithSameRank(countings);
            log.debug("Very important this list is ordered like so: #fourOfAKinds, #threeOfAKinds, #pairs\n{}", cardWithSameRankCombinationCount);

            var handRankDto = processHandRank(hand, countings, ranks, cardWithSameRankCombinationCount);
            if (handRankDto != null) playersWithRank.put(player, handRankDto);
        }
        return playersWithRank;
    }

    private CardPropsCountsDto setupCardPropsCountsDto(List<Card> hand) {
        var rankCounts = new HashMap<Integer, Integer>();
        var suitCounts = new EnumMap<Suit, Integer>(Suit.class);

        hand.forEach(card -> {
            log.info("counting number of cards in hand with same rank");
            rankCounts.put(card.getRank(), rankCounts.getOrDefault(card.getRank(), 0) + 1);
            log.info("counting number of cards in hand with same suit");
            suitCounts.put(card.getSuit(), suitCounts.getOrDefault(card.getSuit(), 0) + 1);
        });
        return new CardPropsCountsDto(rankCounts, suitCounts);
    }

    private List<Integer> countCardsWithSameRank(CardPropsCountsDto countings) {
        int fourOfKind = 0;
        int threeOfKind = 0;
        int pairs = 0;
        for (int count : countings.rankCounts().values()) {
            if (count == 4) fourOfKind++;
            else if (count == 3) threeOfKind++;
            else if (count == 2) pairs++;
        }

        var result = new ArrayList<Integer>();
        result.add(fourOfKind);
        result.add(threeOfKind);
        result.add(pairs);

        return result;
    }

    private HandRankDto processHandRank(List<Card> hand, CardPropsCountsDto countings, List<Integer> ranks, List<Integer> cardWithSameRankCombinationCount) {
        if (hand.isEmpty()) return null;
        log.info("determining hand rank after preparations ");
        int situation = setupProcessHandRanks(hand, ranks, cardWithSameRankCombinationCount, countings);

        switch (situation) {
            case 1 -> {
                return processRoyalFlush(hand);
            }
            case 2 -> {
                return processStraightFlush(hand, ranks);
            }
            case 3 -> {
                return processFourOfKind(hand, countings);
            }
            case 4 -> {
                return processFullHouse(hand, countings);
            }
            case 5 -> {
                return processFlush(hand, countings);
            }
            case 6 -> {
                return processStraight(hand, ranks);
            }
            case 7 -> {
                return processThreeOfKind(hand, countings);
            }
            case 8 -> {
                return processTwoPair(hand, countings);
            }
            case 9 -> {
                return processPair(hand, countings);
            }
            default -> {
                return new HandRankDto(0, new ArrayList<>(), hand);
            }
        }
    }

    private int setupProcessHandRanks(List<Card> hand, List<Integer> ranks, List<Integer> cardWithSameRankCombinationCount, CardPropsCountsDto countings) {
        Map<Supplier<Boolean>, Integer> conditions = new LinkedHashMap<>();
        conditions.put(() -> isRoyalFlush(hand), 1);
        conditions.put(() -> isStraightFlush(hand), 2);
        conditions.put(() -> cardWithSameRankCombinationCount.get(0) > 0, 3);
        conditions.put(() -> isFullHouse(cardWithSameRankCombinationCount), 4);
        conditions.put(() -> isFlush(countings), 5);
        conditions.put(() -> isStraight(ranks), 6);
        conditions.put(() -> cardWithSameRankCombinationCount.get(1) > 0, 7);
        conditions.put(() -> cardWithSameRankCombinationCount.get(2) >= 2, 8);
        conditions.put(() -> cardWithSameRankCombinationCount.get(2) == 1, 9);

        return conditions.entrySet().stream()
                .filter(entry -> entry.getKey().get())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(10);
    }

    private boolean isRoyalFlush(List<Card> hand) {
        // Define the required ranks for a Royal Flush
        List<Integer> royalRanks = List.of(10, 11, 12, 13, 14);

        // Group cards by suit
        Map<Suit, List<Integer>> cardsBySuit = hand.stream()
                .collect(Collectors.groupingBy(
                        Card::getSuit,
                        Collectors.mapping(Card::getRank, Collectors.toList())
                ));

        // Iterate over each suit's cards
        for (var entry : cardsBySuit.entrySet()) {
            List<Integer> suitRanks = entry.getValue();

            // A flush requires at least 5 cards of the same suit
            if (new HashSet<>(suitRanks).containsAll(royalRanks)) {
                return true;
            }
        }

        return false; // No Royal Flush found
    }

    private boolean isStraightFlush(List<Card> hand) {
        Map<Suit, List<Integer>> cardsBySuit = hand.stream()
                .collect(Collectors.groupingBy(
                        Card::getSuit,
                        Collectors.mapping(Card::getRank, Collectors.toList())
                ));

        for (var entry : cardsBySuit.entrySet()) {
            List<Integer> suitRanks = entry.getValue();

            if (suitRanks.size() >= 5) {
                Collections.sort(suitRanks);

                if (isStraight(suitRanks)) {
                    return true;
                }
            }
        }

        return false;
    }

    private HandRankDto processPair(List<Card> hand, CardPropsCountsDto countings) {
        int pairRank = countings.rankCounts().entrySet().stream()
                .filter(entry -> entry.getValue() == 2)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);

        if (pairRank != 0) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() == pairRank)
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() != pairRank)
                    .toList());

            return new HandRankDto(100, combinationCards, nonCombinationCards);
        }
        return null;
    }

    private HandRankDto processTwoPair(List<Card> hand, CardPropsCountsDto countings) {

        ProcessDuoComboDto result = setupProcessTwoPair(countings);

        if (result.firstComboRank() != -1 && result.secondComboRank() != -1) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() == result.firstComboRank() || card.getRank() == result.secondComboRank())
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> !combinationCards.contains(card))
                    .toList());

            return new HandRankDto(200, combinationCards, nonCombinationCards);
        }
        return null;
    }

    private ProcessDuoComboDto setupProcessTwoPair(CardPropsCountsDto countings) {
        var firstPairRank = -1;
        var secondPairRank = -1;

        for (var entry : countings.rankCounts().entrySet()) {
            if (entry.getValue() == 2) {
                if (firstPairRank == -1) {
                    firstPairRank = entry.getKey();
                } else if (secondPairRank == -1) {
                    secondPairRank = entry.getKey();
                }
            }
        }
        return new ProcessDuoComboDto(firstPairRank, secondPairRank);
    }

    private HandRankDto processThreeOfKind(List<Card> hand, CardPropsCountsDto countings) {

        int threeRank = countings.rankCounts().entrySet().stream()
                .filter(entry -> entry.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);

        var combinationCards = new ArrayList<>(hand.stream()
                .filter(card -> card.getRank() == threeRank)
                .toList());

        var nonCombinationCards = new ArrayList<>(hand.stream()
                .filter(card -> card.getRank() != threeRank)
                .toList());
        return new HandRankDto(300, combinationCards, nonCombinationCards);
    }

    private HandRankDto processStraight(List<Card> hand, List<Integer> ranks) {

        var combinationCards = new ArrayList<>(hand.stream()
                .filter(card -> ranks.contains(card.getRank()))
                .toList());

        var nonCombinationCards = new ArrayList<>(hand.stream()
                .filter(card -> !ranks.contains(card.getRank()))
                .toList());

        if (combinationCards.size() >= 5) return new HandRankDto(400, combinationCards, nonCombinationCards);
        return null;
    }

    private HandRankDto processFlush(List<Card> hand, CardPropsCountsDto countings) {

        Suit flushSuit = countings.suitCounts().entrySet().stream()
                .filter(entry -> entry.getValue() >= 5)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (flushSuit != null) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getSuit() == flushSuit)
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getSuit() != flushSuit)
                    .toList());
            return new HandRankDto(500, combinationCards, nonCombinationCards);
        }
        return null;
    }

    private HandRankDto processFullHouse(List<Card> hand, CardPropsCountsDto countings) {

        ProcessDuoComboDto setup = setupProcessFullHouse(countings);

        if (setup.firstComboRank() != 0 && setup.secondComboRank() != 0) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() == setup.firstComboRank() || card.getRank() == setup.secondComboRank())
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() != setup.firstComboRank() || card.getRank() != setup.secondComboRank())
                    .toList());

            return new HandRankDto(600, combinationCards, nonCombinationCards);
        }
        return null;
    }

    private ProcessDuoComboDto setupProcessFullHouse(CardPropsCountsDto countings) {
        int threeRank = countings.rankCounts().entrySet().stream()
                .filter(entry -> entry.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);

        int pairRank = countings.rankCounts().entrySet().stream()
                .filter(entry -> entry.getValue() == 2)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        return new ProcessDuoComboDto(threeRank, pairRank);
    }

    private HandRankDto processFourOfKind(List<Card> hand, CardPropsCountsDto countings) {

        int fourRank = countings.rankCounts().entrySet().stream()
                .filter(entry -> entry.getValue() == 4)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);

        if (fourRank != 0) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() == fourRank)
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> card.getRank() != fourRank)
                    .toList());
            return new HandRankDto(700, combinationCards, nonCombinationCards);
        }
        return null;
    }

    private HandRankDto processStraightFlush(List<Card> hand, List<Integer> ranks) {

        for (int j = 0; j < ranks.size() - 4; j++) {
            List<Integer> sublist = ranks.subList(j, j + 5);

            Suit suit = hand.stream()
                    .filter(card -> sublist.contains(card.getRank()))
                    .map(Card::getSuit)
                    .distinct()
                    .findFirst()
                    .orElse(null);

            if (suit != null) {
                var combinationCards = new ArrayList<>(hand.stream()
                        .filter(card -> sublist.contains(card.getRank()) && card.getSuit() == suit)
                        .toList());

                var nonCombinationCards = new ArrayList<>(hand.stream()
                        .filter(card -> !combinationCards.contains(card))
                        .toList());

                return new HandRankDto(800, combinationCards, nonCombinationCards);
            }
        }
        return null;
    }

    private HandRankDto processRoyalFlush(List<Card> hand) {

        Set<Integer> royalFlushRanks = Set.of(10, 11, 12, 13, 14);

        Suit royalFlushSuit = hand.stream()
                .filter(card -> royalFlushRanks.contains(card.getRank()))
                .map(Card::getSuit)
                .distinct()
                .findFirst()
                .orElse(null);

        if (royalFlushSuit != null) {
            var combinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> royalFlushRanks.contains(card.getRank()) && card.getSuit() == royalFlushSuit)
                    .toList());

            var nonCombinationCards = new ArrayList<>(hand.stream()
                    .filter(card -> !combinationCards.contains(card))
                    .toList());

            return new HandRankDto(900, combinationCards, nonCombinationCards);  // Royal Flush has the highest rank, e.g. 900
        }
        return null;
    }

    private boolean isStraight(List<Integer> ranks) {
        boolean isStraight = false;
        int consecutive = 1;
        for (int j = 1; j < ranks.size(); j++) {
            if (ranks.get(j) == ranks.get(j - 1) + 1) {
                consecutive++;
                if (consecutive >= 5) {
                    isStraight = true;
                    break;
                }
            } else {
                consecutive = 1;
            }
        }

        if (ranks.size() >= 4 && ranks.contains(14) && ranks.subList(0, 4).equals(List.of(2, 3, 4, 5))) {
            isStraight = true;
        }
        return isStraight;
    }

    private boolean isFullHouse(List<Integer> cardWithSameRankCombinationCount) {
        return (cardWithSameRankCombinationCount.get(1) >= 1 && cardWithSameRankCombinationCount.get(2) >= 1);
    }

    private boolean isFlush(CardPropsCountsDto countings) {
        return countings.suitCounts().values().stream().anyMatch(count -> count >= 5);
    }

    public int calculateHandScore(List<Card> hand) {
        hand.sort(Comparator.comparing(Card::getRank).reversed());
        CardPropsCountsDto countings = setupCardPropsCountsDto(hand);
        List<Integer> ranks = hand.stream().map(Card::getRank).distinct().sorted().toList();
        List<Integer> cardWithSameRankCombinationCount = countCardsWithSameRank(countings);
        HandRankDto handRankDto = processHandRank(hand, countings, ranks, cardWithSameRankCombinationCount);
        return handRankDto != null ? handRankDto.score() : 0;
    }
}
