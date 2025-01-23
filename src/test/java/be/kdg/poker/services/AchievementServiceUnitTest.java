package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.AchievementDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Achievement;
import be.kdg.poker.domain.Game;
import be.kdg.poker.domain.Player;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AchievementRepository;
import be.kdg.poker.repositories.PlayerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AchievementServiceUnitTest {

    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private AchievementRepository achievementRepository;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PlayerRepository playerRepository;

    @Autowired
    private AchievementService achievementService;

    @Test
    void getAchievementsByAccountId_ShouldReturnAchievements_WhenAccountHasAchievements() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Achievement achievement = new Achievement();
        achievement.setId(UUID.randomUUID());
        achievement.setName("5 Wins");
        achievement.setDescription("Win 5 games");
        achievement.setPokerPoints(50);

        when(accountRepository.findAllAchievementsByAccountId(accountId)).thenReturn(List.of(achievement));

        // Act
        List<AchievementDto> result = achievementService.getAchievementsByAccountId(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("5 Wins", result.get(0).name());
    }


    @Test
    void getAchievements_ShouldReturnAllAchievements() {
        // Arrange
        Achievement achievement1 = new Achievement();
        achievement1.setId(UUID.randomUUID());
        achievement1.setName("5 Wins");
        achievement1.setDescription("Win 5 games");
        achievement1.setPokerPoints(50);

        Achievement achievement2 = new Achievement();
        achievement2.setId(UUID.randomUUID());
        achievement2.setName("10 Wins");
        achievement2.setDescription("Win 10 games");
        achievement2.setPokerPoints(100);

        when(achievementRepository.findAll()).thenReturn(List.of(achievement1, achievement2));

        // Act
        List<AchievementDto> result = achievementService.getAchievements();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("5 Wins", result.get(0).name());
        assertEquals("10 Wins", result.get(1).name());
    }
    @Test
    void checkAchievements_ShouldUnlockAchievement_WhenNotAlreadyUnlocked() {
        // Arrange
        List<Achievement> unlockedAchievements = new ArrayList<>();
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setPokerPoints(0);

        Achievement achievement = new Achievement();
        achievement.setId(UUID.randomUUID());
        achievement.setName("Test Achievement");
        achievement.setPokerPoints(10);

        Set<Achievement> existingAchievements = new HashSet<>();

        // Mock repository and notification service behavior
        when(accountRepository.save(account)).thenReturn(account); // Mock account saving
        doNothing().when(notificationService).notifyAchievementObtained(any(Account.class), any(Achievement.class)); // Mock notification

        // Act
        achievementService.checkAchievements(unlockedAchievements, account, existingAchievements, achievement);

        // Assert
        assertEquals(1, unlockedAchievements.size());
        assertEquals(10, account.getPokerPoints());
        Assertions.assertTrue(unlockedAchievements.contains(achievement));

        // Verify interactions
        verify(notificationService, times(1)).notifyAchievementObtained(account, achievement);
    }


    @Test
    void checkAchievements_ShouldNotUnlockAchievement_WhenAlreadyUnlocked() {
        // Arrange
        List<Achievement> unlockedAchievements = new ArrayList<>();
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setPokerPoints(0);

        Achievement achievement = new Achievement();
        achievement.setId(UUID.randomUUID());
        achievement.setName("Test Achievement");
        achievement.setPokerPoints(10);

        account.getAchievements().add(achievement);

        Set<Achievement> existingAchievements = new HashSet<>();
        existingAchievements.add(achievement);

        // Act
        achievementService.checkAchievements(unlockedAchievements, account, existingAchievements, achievement);

        // Assert
        assertEquals(0, unlockedAchievements.size());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void checkForAchievementsAtEndOfGame_ShouldReturnUnlockedAchievements_WhenAccountsHaveAchievements() {
        // Arrange
        Account account1 = new Account();
        account1.setId(UUID.randomUUID());
        account1.setPokerPoints(0);
        account1.setCountersAchievements(Map.of("wins", 6));

        Account account2 = new Account();
        account2.setId(UUID.randomUUID());
        account2.setPokerPoints(0);
        account2.setCountersAchievements(Map.of("wins", 12));

        Achievement achievement1 = new Achievement();
        achievement1.setId(UUID.randomUUID());
        achievement1.setName("5 Wins");
        achievement1.setDescription("Win 5 games");
        achievement1.setPokerPoints(50);

        Achievement achievement2 = new Achievement();
        achievement2.setId(UUID.randomUUID());
        achievement2.setName("10 Wins");
        achievement2.setDescription("Win 10 games");
        achievement2.setPokerPoints(100);

        Player player1 = new Player();
        player1.setId(UUID.randomUUID());
        player1.setAccount(account1);
        player1.setGame(new Game());

        Player player2 = new Player();
        player2.setId(UUID.randomUUID());
        player2.setAccount(account2);
        player2.setGame(new Game());

        account2.setAchievements(new HashSet<>(List.of(achievement1)));

        List<Account> accounts = List.of(account1, account2);
        List<Player> players = List.of(player1, player2);

        Game game = new Game();
        game.setId(UUID.randomUUID());

        when(playerRepository.findAllByGameId(game.getId())).thenReturn(players);
        when(accountRepository.findAllById(anyList())).thenReturn(accounts);
        when(achievementRepository.findAll()).thenReturn(List.of(achievement1, achievement2));

        // Act
        List<Achievement> result = achievementService.checkForAchievementsAtEndOfGame(game);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50, account1.getPokerPoints());
        assertEquals(100, account2.getPokerPoints());
    }

    @Test
    void checkForAchievementsAtEndOfGame_ShouldReturnEmptyList_WhenNoAchievements() {
        // Arrange
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setPokerPoints(0);

        List<Account> accounts = List.of(account);

        Game game = new Game();
        game.setId(UUID.randomUUID());

        when(accountRepository.findAll()).thenReturn(accounts);
        when(achievementRepository.findAll()).thenReturn(List.of());

        // Act
        List<Achievement> result = achievementService.checkForAchievementsAtEndOfGame(game);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void checkForAchievementsAtEndOfGame_ShouldReturnEmptyList_WhenNoAccounts() {
        // Arrange
        List<Account> accounts = new ArrayList<>();
        when(accountRepository.findAll()).thenReturn(accounts);

        // Act
        List<Achievement> result = achievementService.checkForAchievementsAtEndOfGame(new Game());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}