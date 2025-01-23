package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.AchievementDto;
import be.kdg.poker.controllers.dto.HandRankDto;
import be.kdg.poker.domain.*;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AchievementRepository;
import be.kdg.poker.repositories.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AccountRepository accountRepository;
    private final PlayerRepository playerRepository;
    private final HandRankService handRankService;
    private final NotificationService notificationService;

    public AchievementService(AchievementRepository achievementRepository, AccountRepository accountRepository, PlayerRepository playerRepository, HandRankService handRankService, NotificationService notificationService) {
        this.achievementRepository = achievementRepository;
        this.accountRepository = accountRepository;
        this.playerRepository = playerRepository;
        this.handRankService = handRankService;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<Achievement> checkForAchievementsAtEndOfGame(Game game) {
        log.info("Checking for achievements for game ID: {}", game.getId());

        List<Player> players = playerRepository.findAllByGameId(game.getId());
        List<UUID> accountIds = players.stream()
                .map(player -> player.getAccount().getId())
                .toList();

        List<Account> accounts = accountRepository.findAllById(accountIds);
        if (accounts.isEmpty()) {
            log.warn("No accounts found for the provided player IDs in game ID: {}", game.getId());
            return List.of();
        }

        List<Achievement> achievements = achievementRepository.findAll();
        List<Achievement> unlockedAchievements = new ArrayList<>();

        for (Account account : accounts) {
            Set<Achievement> existingAchievements = account.getAchievements();

            for (Achievement achievement : achievements) {
                if (isGameAchievementCriteriaMet(account, achievement)) {
                    checkAchievements(unlockedAchievements, account, existingAchievements, achievement);
                }
            }

            accountRepository.save(account);
        }

        return unlockedAchievements;
    }

    @Transactional
    public List<Achievement> checkForHandAchievementsAtEachRound(Game game, Map<Player, List<Card>> hands) {
        log.info("Checking for hand achievements for game ID: {}", game.getId());

        List<Player> players = playerRepository.findAllByGameId(game.getId());


        List<Achievement> unlockedAchievements = new ArrayList<>();

        for (Player player : players) {
            Account account = player.getAccount();
            Set<Achievement> existingAchievements = account.getAchievements();

            for (Achievement achievement : achievementRepository.findAll()) {
                if (isHandAchievementCriteriaMet(account, achievement, hands)) {
                    checkAchievements(unlockedAchievements, account, existingAchievements, achievement);
                }
            }

            accountRepository.save(account);
        }

        return unlockedAchievements;
    }

     public void checkAchievements(List<Achievement> unlockedAchievements, Account account, Set<Achievement> existingAchievements, Achievement achievement) {
        if (!existingAchievements.contains(achievement)) {
            account.getAchievements().add(achievement);
            account.setPokerPoints(account.getPokerPoints() + achievement.getPokerPoints());
            unlockedAchievements.add(achievement);
            notificationService.notifyAchievementObtained(account, achievement);
            log.info("Unlocked achievement '{}' for account ID: {}", achievement.getName(), account.getId());
        }
    }

    private boolean isGameAchievementCriteriaMet(Account account, Achievement achievement) {
        return switch (achievement.getName()) {
            case "5 Wins" -> getCounterValue(account, "wins") >= 5;
            case "10 Wins" -> getCounterValue(account, "wins") >= 10;
            case "20 Wins" -> getCounterValue(account, "wins") >= 20;
            case "50 Wins" -> getCounterValue(account, "wins") >= 50;
            case "100 Wins" -> getCounterValue(account, "wins") >= 100;
            case "At least you tried" -> getCounterValue(account, "playedGames") == 100;
            case "Touch grass" -> getCounterValue(account, "playedGames") == 1000;
            default -> false;
        };
    }

    private int getCounterValue(Account account, String counterName) {
        return account.getCountersAchievements().getOrDefault(counterName, 0);
    }

    private boolean isHandAchievementCriteriaMet(Account account, Achievement achievement, Map<Player, List<Card>> playerHands) {
        return switch (achievement.getName()) {
            case "Royal Flush" -> hasRoyalFlush(account, playerHands);
            case "Flush" -> hasFlush(account, playerHands);
            case "Straight" -> hasStraight(account, playerHands);
            case "Royal Player" -> getCounterValue(account, "royal flushes") >= 10;
            case "Flush God" -> getCounterValue(account, "flushes") >= 50;
            case "Straight Shooter" -> getCounterValue(account, "straights") >= 100;
            default -> false;
        };
    }

    private boolean hasRoyalFlush(Account account, Map<Player, List<Card>> playerHands) {
        List<Player> players = new ArrayList<>(playerHands.keySet());
        Map<Player, HandRankDto> playersWithRank = handRankService.calculateWinnersByHandRanks(players, playerHands);
        boolean hasRoyalFlush = playersWithRank.entrySet().stream()
                .filter(entry -> entry.getValue().score() == 900)
                .anyMatch(entry -> entry.getKey().getAccount().equals(account));

        if (hasRoyalFlush) {
            account.getCountersAchievements().put("royal flushes", account.getCountersAchievements().getOrDefault("royal flushes", 0) + 1);
        }

        return hasRoyalFlush;
    }

    private boolean hasFlush(Account account, Map<Player, List<Card>> playerHands) {
        List<Player> players = new ArrayList<>(playerHands.keySet());
        Map<Player, HandRankDto> playersWithRank = handRankService.calculateWinnersByHandRanks(players, playerHands);
        boolean hasFlush = playersWithRank.entrySet().stream()
                .filter(entry -> entry.getValue().score() == 500)
                .anyMatch(entry -> entry.getKey().getAccount().equals(account));

        if (hasFlush) {
            account.getCountersAchievements().put("flushes", account.getCountersAchievements().getOrDefault("flushes", 0) + 1);
        }

        return hasFlush;
    }

    private boolean hasStraight(Account account, Map<Player, List<Card>> playerHands) {
        List<Player> players = new ArrayList<>(playerHands.keySet());
        Map<Player, HandRankDto> playersWithRank = handRankService.calculateWinnersByHandRanks(players, playerHands);
        boolean hasStraight = playersWithRank.entrySet().stream()
                .filter(entry -> entry.getValue().score() == 400)
                .anyMatch(entry -> entry.getKey().getAccount().equals(account));

        if (hasStraight) {
            account.getCountersAchievements().put("straights", account.getCountersAchievements().getOrDefault("straights", 0) + 1);
        }

        return hasStraight;
    }

    public List<AchievementDto> getAchievementsByAccountId(UUID accountId) {
        List<Achievement> achievements = accountRepository.findAllAchievementsByAccountId(accountId);

        return achievements.stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<AchievementDto> getAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }


    private AchievementDto mapToDto(Achievement achievement) {
        return new AchievementDto(
                achievement.getId(),
                achievement.getName(),
                achievement.getDescription(),
                achievement.getPokerPoints()
        );
    }
}
