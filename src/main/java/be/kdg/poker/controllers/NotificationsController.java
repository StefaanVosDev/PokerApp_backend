package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.*;
import be.kdg.poker.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationsController {
    private final NotificationService notificationService;

    public NotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<NotificationDto> getNotification(@PathVariable UUID id) {
        log.info("Received request to get notification with id {}", id);
        return notificationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.error("unable to find notification with id {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/game/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<GameNotificationDto>> getGameNotifications(@PathVariable String username) {
        log.info("Received request to get game notifications for account with username {}", username);
        var gameNotifications = notificationService.findGameNotificationsByAccountUsername(username);
        return ResponseEntity.ok(gameNotifications);
    }

    @GetMapping("/achievement/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<AchievementNotificationDto>> getAchievementNotifications(@PathVariable String username) {
        log.info("Received request to get achievement notifications for account with username {}", username);
        var achievementNotification = notificationService.findAchievementNotificationsByAccountUsername(username);
        return ResponseEntity.ok(achievementNotification);
    }

    @GetMapping("/invite/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<InviteNotificationDto>> getGameInvites(@PathVariable String username) {
        log.info("Received request to get game invites for account with username {}", username);
        var gameInvites = notificationService.findGameInvitesByAccountUsername(username);
        return ResponseEntity.ok(gameInvites);
    }

    @GetMapping("/friendRequests/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<FriendRequestDto>> getFriendRequests(@PathVariable String username) {
        log.info("Received request to get friend requests for account with username {}", username);
        var friendRequests = notificationService.findFriendRequestsByAccountUsernameNotifications(username);
        return ResponseEntity.ok(friendRequests);
    }
}
