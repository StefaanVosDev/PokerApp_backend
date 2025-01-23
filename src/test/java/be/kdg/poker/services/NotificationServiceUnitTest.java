package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.*;
import be.kdg.poker.domain.*;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.NotificationNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificationServiceUnitTest {

    @MockBean
    private NotificationRepository notificationRepository;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private GameRepository gameRepository;

    @Autowired
    private NotificationService notificationService;

    @Test
    void delete_ShouldDeleteNotification_GivenValidId() {
        UUID id = UUID.randomUUID();
        Notification notification = new Notification();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        notificationService.delete(id);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void delete_ShouldThrowNotificationNotFoundException_GivenInvalidId() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> notificationService.delete(id));
    }

    @Test
    void findById_ShouldReturnNotificationDto_GivenValidId() {
        UUID id = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(id);
        notification.setMessage("Test message");
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        Optional<NotificationDto> result = notificationService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
        assertEquals("Test message", result.get().message());
    }

    @Test
    void findById_ShouldReturnEmpty_GivenInvalidId() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<NotificationDto> result = notificationService.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findFriendRequestsByAccountUsername_ShouldReturnFriendRequestDtos() {
        String username = "testUser";
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestingFriend(new Account());
        friendRequest.getRequestingFriend().setUsername(username);
        friendRequest.setTimestamp(LocalDateTime.now());
        when(notificationRepository.findFriendRequestByAccountUsername(username)).thenReturn(List.of(friendRequest));

        List<FriendRequestDto> result = notificationService.findFriendRequestsByAccountUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).requestingFriendUsername());
    }

    @Test
    void notifyPlayerOnMove_ShouldSaveNotification_GivenValidPlayerAndGame() {
        Player player = new Player();
        player.setUsername("testPlayer");
        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setName("Test Game");
        Account account = new Account();
        account.setUsername("testPlayer");

        when(accountRepository.findAccountByUsername(player.getUsername())).thenReturn(Optional.of(account));

        notificationService.notifyPlayerOnMove(player, game);

        verify(notificationRepository).save(any(GameNotification.class));
    }

    @Test
    void notifyPlayerOnMove_ShouldThrowAccountNotFoundException_GivenInvalidPlayer() {
        Player player = new Player();
        player.setUsername("invalidPlayer");
        Game game = new Game();
        game.setId(UUID.randomUUID());
        game.setName("Test Game");

        when(accountRepository.findAccountByUsername(player.getUsername())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> notificationService.notifyPlayerOnMove(player, game));
    }

    @Test
    void notifyAchievementObtained_ShouldSaveNotification_GivenValidAccountAndAchievement() {
        Account account = new Account();
        account.setUsername("testAccount");
        Achievement achievement = new Achievement();
        achievement.setName("Test Achievement");

        notificationService.notifyAchievementObtained(account, achievement);

        verify(notificationRepository).save(any(AchievementNotification.class));
    }

    @Test
    void findGameNotificationsByAccountUsername_ShouldReturnGameNotificationDtos() {
        String username = "testUser";
        GameNotification gameNotification = new GameNotification();
        gameNotification.setAccount(new Account());
        gameNotification.getAccount().setUsername(username);
        gameNotification.setGame(new Game());
        gameNotification.setTimestamp(LocalDateTime.now());

        when(notificationRepository.findGameNotificationsByAccountUsername(username)).thenReturn(List.of(gameNotification));

        List<GameNotificationDto> result = notificationService.findGameNotificationsByAccountUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).accountUsername());
    }

    @Test
    void findAchievementNotificationsByAccountUsername_ShouldReturnAchievementNotificationDtos() {
        String username = "testUser";
        AchievementNotification achievementNotification = new AchievementNotification();
        achievementNotification.setAccount(new Account());
        achievementNotification.getAccount().setUsername(username);
        achievementNotification.setAchievement(new Achievement());
        achievementNotification.setTimestamp(LocalDateTime.now());

        when(notificationRepository.findAchievementNotificationsByAccountUsername(username)).thenReturn(List.of(achievementNotification));

        List<AchievementNotificationDto> result = notificationService.findAchievementNotificationsByAccountUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).accountUsername());
    }

    @Test
    void inviteFriends_ShouldSaveNotifications_GivenValidGameAndFriends() {
        String username = "testUser";
        UUID gameId = UUID.randomUUID();
        List<String> friendUsernames = List.of("friend1", "friend2");
        Game game = new Game();
        game.setId(gameId);
        game.setName("Test Game");
        Account friend1 = new Account();
        friend1.setUsername("friend1");
        Account friend2 = new Account();
        friend2.setUsername("friend2");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(accountRepository.findAccountByUsername("friend1")).thenReturn(Optional.of(friend1));
        when(accountRepository.findAccountByUsername("friend2")).thenReturn(Optional.of(friend2));

        notificationService.inviteFriends(username, gameId, friendUsernames);

        verify(notificationRepository, times(2)).save(any(InviteNotification.class));
    }

    @Test
    void inviteFriends_ShouldThrowGameNotFoundException_GivenInvalidGameId() {
        String username = "testUser";
        UUID gameId = UUID.randomUUID();
        List<String> friendUsernames = List.of("friend1", "friend2");

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> notificationService.inviteFriends(username, gameId, friendUsernames));
    }

    @Test
    void inviteFriends_ShouldThrowAccountNotFoundException_GivenInvalidFriendUsername() {
        String username = "testUser";
        UUID gameId = UUID.randomUUID();
        List<String> friendUsernames = List.of("friend1", "invalidFriend");
        Game game = new Game();
        game.setId(gameId);
        game.setName("Test Game");
        Account friend1 = new Account();
        friend1.setUsername("friend1");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(accountRepository.findAccountByUsername("friend1")).thenReturn(Optional.of(friend1));
        when(accountRepository.findAccountByUsername("invalidFriend")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> notificationService.inviteFriends(username, gameId, friendUsernames));
    }

    @Test
    void findGameInvitesByAccountUsername_ShouldReturnInviteNotificationDtos() {
        String username = "testUser";
        InviteNotification inviteNotification = new InviteNotification();
        inviteNotification.setAccount(new Account());
        inviteNotification.getAccount().setUsername(username);
        inviteNotification.setGame(new Game());
        inviteNotification.setTimestamp(LocalDateTime.now());
        when(notificationRepository.findGameInvitesByAccountUsername(username)).thenReturn(List.of(inviteNotification));

        List<InviteNotificationDto> result = notificationService.findGameInvitesByAccountUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).accountUsername());
    }
}

