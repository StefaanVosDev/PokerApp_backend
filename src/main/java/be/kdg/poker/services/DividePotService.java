package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.*;
import be.kdg.poker.domain.Card;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.domain.Turn;
import be.kdg.poker.domain.enums.PlayerStatus;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.InvalidWinnerException;
import be.kdg.poker.exceptions.RoundNotFoundException;
import be.kdg.poker.exceptions.TurnNotFoundException;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.PlayerRepository;
import be.kdg.poker.repositories.RoundRepository;
import be.kdg.poker.repositories.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DividePotService {
    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final TurnRepository turnRepository;
    private final PlayerRepository playerRepository;
    private final HandRankService handRankService;
    private final AchievementService achievementService;

    public DividePotService(RoundRepository roundRepository, GameRepository gameRepository, TurnRepository turnRepository, PlayerRepository playerRepository, HandRankService handRankService, AchievementService achievementService) {
        this.roundRepository = roundRepository;
        this.gameRepository = gameRepository;
        this.turnRepository = turnRepository;
        this.playerRepository = playerRepository;
        this.handRankService = handRankService;
        this.achievementService = achievementService;
    }

    @Transactional
    public Optional<CalculateRoundWinnerDto> getCalculateRoundWinnerDto(UUID id) {
        log.info("successfully started constructing round-winner calculation dto");
        log.info("getting round with id {} and community cards", id);
        var round = roundRepository.findByIdWithCommunityCards(id)
                .orElseThrow(() -> new RoundNotFoundException("round with id " + id + " does not exist"));
        log.info("getting game from round");
        var game = gameRepository.findByRoundId(id)
                .orElseThrow(() -> new GameNotFoundException("round with id " + id + " does not have a game"));
        log.info("getting turns from round");
        var turnsWithPlayers = turnRepository.findByRoundIdWithPlayer(id);
        if (turnsWithPlayers.isEmpty()) throw new TurnNotFoundException("round with id " + id + " does not have any turns");
        round.setTurns(turnsWithPlayers);
        log.info("getting hands for players of game with id {}", game.getId());
        var players = playerRepository.findByGameIdWithHands(game.getId());
        game.setPlayers(players);


        log.info("updating stakes in analysis dto");
        var stakes = new HashMap<AllInDto, Integer>();
        players.forEach(p -> {
            var stake = new AtomicInteger();
            var isAllIn = new AtomicBoolean(false);
            turnsWithPlayers.forEach(t -> {
                if (t.getPlayer().getId().equals(p.getId())) {
                    stake.addAndGet(t.getMoneyGambled());
                }
                if (t.getMoveMade().equals(PlayerStatus.ALL_IN)) isAllIn.set(true);
            });
            stakes.put(new AllInDto(p, isAllIn.get()), stake.get());
        });

        log.info("removing players who folded this round from analysis");
        players = getPlayersNotFolded(turnsWithPlayers, players);
        var hands = new HashMap<Player, List<Card>>();
        log.info("updating hands in analysis dto");
        players.forEach(p -> hands.put(p, new ArrayList<>(p.getHand())));
        var communityCards = round.getCommunityCards();
        hands.values().forEach(h -> h.addAll(communityCards));

        log.info("successfully constructed analysis dto");
        return Optional.of(new CalculateRoundWinnerDto(players, hands, stakes));
    }

    private List<Player> getPlayersNotFolded(List<Turn> turnsWithPlayer, List<Player> players) {
        List<Player> allPlayersInRound = new ArrayList<>(players);

        for (Turn turn : turnsWithPlayer) {
            if (turn.getMoveMade().equals(PlayerStatus.FOLD)) {
                allPlayersInRound.remove(turn.getPlayer());
            }
        }
        return allPlayersInRound;
    }

    @Transactional
    public List<List<Player>> calculateWinners(CalculateRoundWinnerDto calcRoundWinnerDto) {
        log.info("successfully started analysis of round-winner based on previously constructed dto");
        Map<Player, HandRankDto> playersWithHandRanks;
        List<List<Player>> playersByWinIndex = new ArrayList<>();
        var players = calcRoundWinnerDto.players();
        var hands = calcRoundWinnerDto.hands();
        log.info("starting hand rank analysis");
        playersWithHandRanks = handRankService.calculateWinnersByHandRanks(players, hands);

        Game game = gameRepository.findGameByPlayerId(players.get(0).getId())
               .orElseThrow(() -> new GameNotFoundException("Game not found"));

       var unlockedAchievements = achievementService.checkForHandAchievementsAtEachRound(game, hands);

       log.info("unlocked achievements: {}", unlockedAchievements);

        var setup = calculateWinnerSetup(playersWithHandRanks);

        if (setup.hasTies() && !setup.onlyRoyalFlushTies()) {
            log.info("starting tiebreaker analysis");
            var result = tieBreaker(setup.scoreGroups(), playersWithHandRanks, playersByWinIndex);
            log.info("successfully concluded analysis of round-winner through tiebreaker analysis");
            return result;
        }

        log.info("starting processing hand ranks without ties");
        var result = processNonTie(playersWithHandRanks, playersByWinIndex);
        log.info("successfully concluded analysis of round-winner without tiebreaker analysis");

        return result;
    }

    private CalculateWinnerSetupDto calculateWinnerSetup(Map<Player, HandRankDto> playersWithHandRanks) {
        log.info("extracting groups of players with same score");
        Map<Integer, List<Player>> scoreGroups = new HashMap<>();
        playersWithHandRanks.forEach((player, handRank) ->
                scoreGroups.computeIfAbsent(handRank.score(), k -> new ArrayList<>()).add(player)
        );

        log.info("determining if round has ties");
        boolean hasTies = scoreGroups.values().stream().anyMatch(group -> group.size() > 1);
        boolean onlyRoyalFlushTies = scoreGroups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .allMatch(entry -> entry.getKey() == 900);
        return new CalculateWinnerSetupDto(scoreGroups, hasTies, onlyRoyalFlushTies);
    }

    private List<List<Player>> processNonTie(Map<Player, HandRankDto> playersWithHandRanks, List<List<Player>> playersByWinIndex) {
        log.info("successfully started processing hand ranks without ties");
        log.info("sorting players with hand ranks based on score");
        var sortedPlayersWithHandRanks = sortPlayersWithHandRanksByHandRankScore(playersWithHandRanks);
        var playersByWinIndexSingle = new ArrayList<Player>();
        for (Map.Entry<Player, HandRankDto> entry : sortedPlayersWithHandRanks) {
            playersByWinIndexSingle.add(entry.getKey());
        }

        log.info("adding players to resulted list of winners by index after being sorted on score");
        for (var player : playersByWinIndexSingle) {
            var playersByWinIndexEntry = new ArrayList<Player>();
            playersByWinIndexEntry.add(player);
            playersByWinIndex.add(playersByWinIndexEntry);
        }
        log.info("successfully processed hand ranks without ties");
        return playersByWinIndex;
    }

    private List<Map.Entry<Player, HandRankDto>> sortPlayersWithHandRanksByHandRankScore(Map<Player, HandRankDto> playersWithHandRanks) {
        List<Map.Entry<Player, HandRankDto>> entryList = new ArrayList<>(playersWithHandRanks.entrySet());

        entryList.sort(Comparator.comparing((Map.Entry<Player, HandRankDto> entry) -> entry.getValue().score()).reversed());

        return entryList;
    }

    private List<List<Player>> tieBreaker(Map<Integer, List<Player>> scoreGroups, Map<Player, HandRankDto> playersWithHandRanks, List<List<Player>> playersByWinIndex) {
        log.info("successfully started tiebreaker analysis");
        log.info("sorting score groups");
        List<Map.Entry<Integer, List<Player>>> sortedScoreGroups = scoreGroups.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                .toList();

        log.info("starting score group analysis");
        for (var scoreGroup : sortedScoreGroups) {
            log.info("analysis of group with score {}", scoreGroup.getKey());
            var playersInGroup = scoreGroup.getValue();

            if (playersInGroup.size() > 1) {
                var tiedPlayersWithHandRanks = playersInGroup.stream()
                        .collect(Collectors.toMap(player -> player, playersWithHandRanks::get));

                log.info("starting analysis of hand rank variation");
                var resolvedTiedPlayers = calculateWinnersByHandRankVariation(tiedPlayersWithHandRanks);

                playersByWinIndex.addAll(resolvedTiedPlayers);
            } else {
                playersByWinIndex.add(playersInGroup);
            }
        }
        log.info("successfully concluded tiebreaker analysis");
        return playersByWinIndex;
    }

    private List<List<Player>> calculateWinnersByHandRankVariation(Map<Player, HandRankDto> playersWithHandRank) {
        log.info("successfully started analysis of hand rank variation of score {}", playersWithHandRank.values().stream().findFirst().get().score());
        var players = new ArrayList<>(playersWithHandRank.keySet().stream().toList());
        var handRanks = playersWithHandRank.values().stream().toList();
        var playersByWinIndex = new ArrayList<List<Player>>();

        log.info("determining biggest hand");
        int maxCombinationHandSize = handRanks.stream()
                .mapToInt(handRank -> handRank.cardsWithCombination().size())
                .max()
                .orElse(0);

        log.info("sorting players by highest card");
        players.sort((p1, p2) -> comparePlayers(p1, p2, playersWithHandRank, maxCombinationHandSize));

        log.info("assembling remaining ties and splitting players after tiebreaker of group");
        List<Player> currentGroup = new ArrayList<>();
        Player previousPlayer = null;

        for (Player player : players) {
            if (previousPlayer == null || comparePlayers(previousPlayer, player, playersWithHandRank, maxCombinationHandSize) == 0) {
                currentGroup.add(player);
            } else {
                playersByWinIndex.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(player);
            }
            previousPlayer = player;
        }

        if (!currentGroup.isEmpty()) {
            playersByWinIndex.add(currentGroup);
        }

        log.info("successfully concluded analysis of hand rank variation of score {}", playersWithHandRank.values().stream().findFirst().get().score());
        return playersByWinIndex;
    }

    private int comparePlayers(Player p1, Player p2, Map<Player, HandRankDto> playersWithHandRank, int maxCombinationHandSize) {
        log.info("comparing player with id {} against player with id {} for highest card", p1.getId(), p2.getId());
        int result;

        result = compareForBetterCard(p1, p2, playersWithHandRank, maxCombinationHandSize);
        if (result != 0) return result;

        result = compareEqualCards(p1, p2, playersWithHandRank);
        return result;
    }

    private int compareEqualCards(Player p1, Player p2, Map<Player, HandRankDto> playersWithHandRank) {
        log.info("comparing cards outside of combination");
        CompareEqualCardsDto setup = setupCompareEqualCards(p1, p2, playersWithHandRank);

        for (int cardIndex = 0; cardIndex < setup.maxNonCombinationCards(); cardIndex++) {
            int p1CardRank = cardIndex < setup.p1NonCombinationRanks().size() ? setup.p1NonCombinationRanks().get(cardIndex) : Integer.MIN_VALUE;
            int p2CardRank = cardIndex < setup.p2NonCombinationRanks().size() ? setup.p2NonCombinationRanks().get(cardIndex) : Integer.MIN_VALUE;

            if (p1CardRank > p2CardRank) {
                return -1;
            } else if (p1CardRank < p2CardRank) {
                return 1;
            }
        }
        return 0;
    }

    private CompareEqualCardsDto setupCompareEqualCards(Player p1, Player p2, Map<Player, HandRankDto> playersWithHandRank) {
        var p1NonCombinationHand = playersWithHandRank.get(p1).cardsWithCombination();
        var p2NonCombinationHand = playersWithHandRank.get(p2).cardsWithoutCombination();

        int maxNonCombinationCards = 5 - playersWithHandRank.get(p1).cardsWithCombination().size();

        // Sort and pick the top `maxNonCombinationCards`
        List<Integer> p1NonCombinationRanks = p1NonCombinationHand.stream()
                .map(Card::getRank)
                .sorted(Comparator.reverseOrder())
                .limit(maxNonCombinationCards)
                .toList();
        List<Integer> p2NonCombinationRanks = p2NonCombinationHand.stream()
                .map(Card::getRank)
                .sorted(Comparator.reverseOrder())
                .limit(maxNonCombinationCards)
                .toList();
        return new CompareEqualCardsDto(maxNonCombinationCards, p1NonCombinationRanks, p2NonCombinationRanks);
    }

    private int compareForBetterCard(Player p1, Player p2, Map<Player, HandRankDto> playersWithHandRank, int maxCombinationHandSize) {
        log.info("comparing cards in combination");
        for (int cardIndex = 0; cardIndex < maxCombinationHandSize; cardIndex++) {
            var p1HandRank = playersWithHandRank.get(p1).cardsWithCombination();
            var p2HandRank = playersWithHandRank.get(p2).cardsWithCombination();

            int p1CardRank = cardIndex < p1HandRank.size() ? p1HandRank.get(cardIndex).getRank() : Integer.MIN_VALUE;
            int p2CardRank = cardIndex < p2HandRank.size() ? p2HandRank.get(cardIndex).getRank() : Integer.MIN_VALUE;

            if (p1CardRank > p2CardRank) {
                return -1;
            } else if (p1CardRank < p2CardRank) {
                return 1;
            }
        }
        return 0;
    }

    @Transactional
    public Map<Player, Integer> dividePot(List<List<Player>> playersByWinIndex, CalculateRoundWinnerDto calcRoundWinnerDto) throws InvalidWinnerException {
        log.info("successfully started dividing pot over winners");
        var firstPlacePlayers = playersByWinIndex.get(0);
        var allInDtoList = calcRoundWinnerDto.stakes().keySet().stream().toList();
        AtomicBoolean roundContainsAllIn = new AtomicBoolean(false);

        firstPlacePlayers.forEach(p -> {
            var allInDto = allInDtoList.stream().filter(dto -> dto.player().equals(p)).findFirst().orElse(null);
            if (allInDto != null && allInDto.allIn()) roundContainsAllIn.set(true);
        });
        if (roundContainsAllIn.get()) {
            log.info("starting dividing pot over winning tree");
            var result = dividePotOverAllWinners(playersByWinIndex, firstPlacePlayers, calcRoundWinnerDto.stakes());
            log.info("successfully divided pot over winning tree");
            return result;
        }
        log.info("starting dividing pot over first place players");
        return dividePotOverOnlyFirstPlace(firstPlacePlayers, calcRoundWinnerDto.stakes());
    }

    @Transactional
    public Map<Player, Integer> dividePotOverAllWinners(List<List<Player>> playersByWinIndex, List<Player> firstPlacePlayers, Map<AllInDto, Integer> stakes) throws InvalidWinnerException {
        if (firstPlacePlayers.isEmpty()) throw new InvalidWinnerException("The winner group is empty");

        var totalPot = stakes.values().stream().mapToInt(Integer::intValue).sum();
        var stakeValues = stakes.values().stream().toList();
        var numWinners = firstPlacePlayers.size();
        var potPerWinner = totalPot / numWinners;
        var allInDtoList = stakes.keySet().stream().toList();
        var smallestPotToClaim = Integer.MAX_VALUE;

        smallestPotToClaim = calculateSmallestPotToClaimByFirstPlacePlayers(firstPlacePlayers, stakes, stakeValues, allInDtoList, smallestPotToClaim);

        if (smallestPotToClaim >= potPerWinner) return divideEquallyOverFirstPlacePlayers(firstPlacePlayers, potPerWinner);

        return dividePotTakingInAccountAllInStakes(playersByWinIndex, stakes, totalPot, stakeValues, allInDtoList);
    }

    private int calculateSmallestPotToClaimByFirstPlacePlayers(List<Player> firstPlacePlayers, Map<AllInDto, Integer> stakes, List<Integer> stakeValues, List<AllInDto> allInDtoList, int smallestPotToClaim) {
        for (var winner : firstPlacePlayers) {
            var maxPotToClaim = 0;
            var allInDto = allInDtoList.stream().filter(dto -> dto.player().equals(winner)).findFirst().orElse(null);
            if (allInDto != null) {
                var currentStake = stakes.get(allInDto);
                for (var stake : stakeValues) {
                    if (stake <= currentStake) maxPotToClaim += stake;
                    else maxPotToClaim += currentStake;
                }
                if (maxPotToClaim < smallestPotToClaim) smallestPotToClaim = maxPotToClaim;
            }
        }
        return smallestPotToClaim;
    }

    @Transactional
    public Map<Player, Integer> dividePotTakingInAccountAllInStakes(List<List<Player>> playersByWinIndex, Map<AllInDto, Integer> stakes, int totalPot, List<Integer> stakeValues, List<AllInDto> allInDtoList) {
        var result = new HashMap<Player, Integer>();
        var remainingPot = totalPot;

        for (var group : playersByWinIndex) {
            if (remainingPot > 0) {
                remainingPot = dividePotShareOverGroup(stakes, stakeValues, allInDtoList, result, remainingPot, group);
            }
        }

        return result;
    }

    @Transactional
    public int dividePotShareOverGroup(Map<AllInDto, Integer> stakes, List<Integer> stakeValues, List<AllInDto> allInDtoList, Map<Player, Integer> result, int remainingPot, List<Player> group) {
        if (!group.isEmpty()) {
            var potPerPlayer = remainingPot / group.size();
            for (var player : group) {
                var allInDto = allInDtoList.stream().filter(dto -> dto.player().equals(player)).findFirst().orElse(null);
                if (allInDto != null) {
                    var currentStake = stakes.get(allInDto);
                    var maxPotToClaim = stakeValues.stream()
                            .mapToInt(stake -> Math.min(stake, currentStake))
                            .sum();

                    var amountToGive = Math.min(maxPotToClaim, potPerPlayer);
                    player.setMoney(player.getMoney() + amountToGive);
                    result.put(player, amountToGive);
                    remainingPot -= amountToGive;

                    playerRepository.save(player);
                }
            }
        }
        return remainingPot;
    }


    @Transactional
    public Map<Player, Integer> dividePotOverOnlyFirstPlace(List<Player> firstPlacePlayers, Map<AllInDto, Integer> stakes) throws InvalidWinnerException {
        log.info("successfully started dividing pot over first place players");
        var totalPot = stakes.values().stream().mapToInt(Integer::intValue).sum();

        if (firstPlacePlayers.isEmpty()) throw new InvalidWinnerException("The winner group is empty");
        int numWinners = firstPlacePlayers.size();
        int potPerPlayer = totalPot / numWinners;

        var result = divideEquallyOverFirstPlacePlayers(firstPlacePlayers, potPerPlayer);
        log.info("successfully divided pot over first place players");
        return result;
    }

    @Transactional
    public Map<Player, Integer> divideEquallyOverFirstPlacePlayers(List<Player> firstPlacePlayers, int potPerPlayer) {
        for (Player player : firstPlacePlayers) {
            log.info("updating money of player with id {}. Adding ${}", player.getId(), potPerPlayer);
            log.info("money before: {}", player.getMoney());
            player.setMoney(player.getMoney() + potPerPlayer);
            player = playerRepository.save(player);
            log.info("money after {}", player.getMoney());
            firstPlacePlayers.set(firstPlacePlayers.indexOf(player), player);
        }

        var results = new HashMap<Player, Integer>();

        for (Player player : firstPlacePlayers) {
            results.put(player, potPerPlayer);
        }

        return results;
    }
}
