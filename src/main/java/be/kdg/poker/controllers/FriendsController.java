package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.FriendRequestDto;
import be.kdg.poker.controllers.dto.FriendsListDto;
import be.kdg.poker.exceptions.*;
import be.kdg.poker.exceptions.FriendRequestAlreadyExistsException.Type;
import be.kdg.poker.services.DirectMessageService;
import be.kdg.poker.services.FriendService;
import be.kdg.poker.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@Slf4j
public class FriendsController {
    private final FriendService friendService;
    private final NotificationService notificationService;
    private final DirectMessageService directMessageService;

    public FriendsController(FriendService friendService, NotificationService notificationService, DirectMessageService directMessageService) {
        this.friendService = friendService;
        this.notificationService = notificationService;
        this.directMessageService = directMessageService;
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<FriendsListDto>> getFriends(@PathVariable String username) {
        log.info("Received request to get all friends");
        List<FriendsListDto> friends = friendService.getFriends(username);
        if (friends.isEmpty()) {
            log.error("username {} doesn't have any friends", username);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{username}/{friendUsername}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> deleteFriend(@PathVariable String username, @PathVariable String friendUsername) {
        log.info("Received request to delete {}'s friend with username {}", username, friendUsername);
        friendService.deleteFriend(username, friendUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/{friendUsername}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<String> requestFriend(@PathVariable String friendUsername, @PathVariable String username) {
        log.info("Received request to create friend request for {} from {}", friendUsername, username);
        try {
            var friendRequest = friendService.createFriendRequest(username, friendUsername);
            return ResponseEntity.ok(friendRequest.message());
        } catch (AccountNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend with username " + friendUsername + " not found");
        } catch (FriendAlreadyAddedException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Player with username " + friendUsername + " is already your friend");
        } catch (FriendRequestAlreadyExistsException e) {
            log.error(e.getMessage());
            if (e.getType() == Type.ACCOUNT_TO_FRIEND) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Player with username " + friendUsername + " already sent you a request");
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("You have already sent a request to player with username " + friendUsername);
            }
        } catch (LoggedInUserNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (CannotAddOwnAccountException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).build();
        }
    }

    @PutMapping("/accept")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<String> acceptRequest(@RequestParam UUID friendRequestId) {
        log.info("Received request to accept friend request with id {}", friendRequestId);
        try {
            friendService.acceptRequest(friendRequestId);
            return ResponseEntity.ok().build();
        } catch (NotificationNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/decline")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<String> declineRequest(@RequestParam UUID friendRequestId) {
        log.info("Received request to decline friend request with id {}", friendRequestId);
        try {
            log.info("successfully declined friend request with id {}", friendRequestId);
            notificationService.delete(friendRequestId);
            return ResponseEntity.ok().build();
        } catch (NotificationNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requests/{accountUsername}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<FriendRequestDto>> getFriendRequestsFromAccountUsername(@PathVariable String accountUsername) {
        log.info("Received request to get friend requests from account with username {}", accountUsername);
        var requests = notificationService.findFriendRequestsByAccountUsername(accountUsername);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/invite/{username}/{gameId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> inviteFriendToGame(@PathVariable String username, @PathVariable UUID gameId, @RequestBody List<String> friendUsernames) {
        log.info("Received request to invite friends with usernames {} to game with id {}", friendUsernames, gameId);
        notificationService.inviteFriends(username, gameId, friendUsernames);
        directMessageService.sendGameInvites(username, gameId, friendUsernames);

        return ResponseEntity.ok().build();
    }
}
