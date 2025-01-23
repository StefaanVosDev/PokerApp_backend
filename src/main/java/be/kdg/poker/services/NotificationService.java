package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.*;
import be.kdg.poker.domain.*;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.GameNotFoundException;
import be.kdg.poker.exceptions.NotificationNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.GameRepository;
import be.kdg.poker.repositories.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;

    public NotificationService(NotificationRepository notificationRepository, AccountRepository accountRepository, GameRepository gameRepository) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
        this.gameRepository = gameRepository;
    }

    public void delete(UUID id) throws NotificationNotFoundException {
        var friendRequest = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("unable to find friend request with id " + id));
        notificationRepository.delete(friendRequest);
    }

    public Optional<NotificationDto> findById(UUID id) {
        return notificationRepository.findById(id).map(notification -> new NotificationDto(notification.getId(), notification.getMessage()));
    }

    public List<FriendRequestDto> findFriendRequestsByAccountUsername(String accountUsername) {
        return mapToFriendRequestDtos(notificationRepository.findFriendRequestByAccountUsername(accountUsername));
    }

    private List<FriendRequestDto> mapToFriendRequestDtos(List<FriendRequest> friendRequests) {
        return friendRequests.stream().map(
                        request -> new FriendRequestDto(request.getId(), request.getRequestingFriend().getUsername(),
                                request.getMessage(), request.getTimestamp())
                )
                .toList();
    }

    public void notifyPlayerOnMove(Player player, Game game) {
        Account account = accountRepository.findAccountByUsername(player.getUsername())
                .orElseThrow(() -> new AccountNotFoundException("Unable to find account with username " + player.getUsername()));

        GameNotification notification = new GameNotification(game, "It's your turn in game " + game.getName(), account);
        log.info("Notifying player {} on move in game {}", player.getUsername(), game.getId());
        notificationRepository.save(notification);
    }

    public void notifyAchievementObtained(Account account, Achievement achievement) {
        AchievementNotification notification = new AchievementNotification(achievement, "You obtained a achievement " + achievement.getName(), account);
        log.info("Notifying account {} obtaining {}", account.getUsername(), achievement.getName());
        notificationRepository.save(notification);
    }

    public List<GameNotificationDto> findGameNotificationsByAccountUsername(String accountUsername) {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return mapToGameNotificationDto(notificationRepository.findGameNotificationsByAccountUsername(accountUsername)
                .stream()
                .filter(notification -> notification.getTimestamp().isAfter(tenSecondsAgo))
                .toList());
    }

    public List<AchievementNotificationDto> findAchievementNotificationsByAccountUsername(String accountUsername) {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return mapToAchievementNotificationDto(notificationRepository.findAchievementNotificationsByAccountUsername(accountUsername)
                .stream()
                .filter(notification -> notification.getTimestamp().isAfter(tenSecondsAgo))
                .toList());
    }

    private List<GameNotificationDto> mapToGameNotificationDto(List<GameNotification> gameNotifications) {
        return gameNotifications.stream().map(
                        notification -> new GameNotificationDto(notification.getId(), mapToGameDto(notification.getGame()),
                                notification.getMessage(), notification.getAccount().getUsername(), notification.getTimestamp())
                )
                .toList();
    }

    private List<AchievementNotificationDto> mapToAchievementNotificationDto(List<AchievementNotification> achievementNotifications) {
        return achievementNotifications.stream().map(
                        notification -> new AchievementNotificationDto(notification.getId(), notification.getAchievement().getName(),
                                notification.getMessage(), notification.getAccount().getUsername(), notification.getTimestamp())
                )
                .toList();
    }

    private GameDto mapToGameDto(Game game) {
        return new GameDto(game.getId(), game.getStatus(), game.getMaxPlayers(), null, null, null, game.getName(), null);
    }

    public void inviteFriends(String username, UUID gameId, List<String> friendUsernames) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Unable to find game with id " + gameId));

        for (String friendUsername : friendUsernames) {
            Account friendAccount = accountRepository.findAccountByUsername(friendUsername)
                    .orElseThrow(() -> new AccountNotFoundException("Unable to find account with username " + friendUsername));

            InviteNotification notification = new InviteNotification(game, "You have been invited to game " + game.getName() + " by " + username, friendAccount, username);
            notificationRepository.save(notification);
        }
    }

    public List<InviteNotificationDto> findGameInvitesByAccountUsername(String accountUsername) {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return mapToInviteNotificationDto(notificationRepository.findGameInvitesByAccountUsername(accountUsername)
                .stream()
                .filter(notification -> notification.getTimestamp().isAfter(tenSecondsAgo))
                .toList());
    }

    private List<InviteNotificationDto> mapToInviteNotificationDto(List<InviteNotification> inviteNotifications) {
        return inviteNotifications.stream().map(
                        notification -> new InviteNotificationDto(notification.getId(), mapToGameDto(notification.getGame()),
                                notification.getMessage(), notification.getAccount().getUsername(), notification.getTimestamp(), notification.getSender())
                )
                .toList();
    }

    public List<FriendRequestDto> findFriendRequestsByAccountUsernameNotifications(String accountUsername) {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        return mapToFriendRequestDtos(notificationRepository.findFriendRequestByAccountUsername(accountUsername)
                .stream()
                .filter(notification -> notification.getTimestamp().isAfter(tenSecondsAgo))
                .toList());
    }
}


