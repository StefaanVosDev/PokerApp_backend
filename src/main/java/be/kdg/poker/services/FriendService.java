package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.FriendRequestDto;
import be.kdg.poker.controllers.dto.FriendsListDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.FriendRequest;
import be.kdg.poker.exceptions.*;
import be.kdg.poker.exceptions.FriendRequestAlreadyExistsException.Type;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FriendService {
    private final AccountRepository accountRepository;
    private final NotificationRepository friendRequestRepository;

    public FriendService(AccountRepository accountRepository, NotificationRepository friendRequestRepository) {
        this.accountRepository = accountRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    @Transactional
    public void deleteFriend(String username, String friendUsername) {
        log.info("Deleting {}'s friend with username {}", username, friendUsername);
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + username + " not found"));
        List<Account> friends = account.getFriends();

        Account friendAccount = accountRepository.findAccountByUsername(friendUsername)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + friendUsername + " not found"));
        List<Account> friendFriends = friendAccount.getFriends();

        friends.removeIf(friend -> friend.getUsername().equals(friendUsername));
        friendFriends.removeIf(friend -> friend.getUsername().equals(username));

        accountRepository.save(account);
        accountRepository.save(friendAccount);
        log.info("Friend with username {} successfully removed from account {}", friendUsername, username);
    }

    @Transactional
    public List<FriendsListDto> getFriends(String username) {
        log.info("Getting all friends for user with username {}", username);
        Account account = accountRepository.findAccountByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("Account with username " + username + " not found"));

        List<FriendsListDto> friends = new ArrayList<>();
        for (Account friend : account.getFriends()) {
            log.info("Getting all friends for user with username {}", friend.getUsername());
            FriendsListDto friendsListDto = new FriendsListDto(friend.getUsername(), friend.getActiveAvatar().getImage());
            friends.add(friendsListDto);
        }

        return friends;
    }

    @Transactional
    public FriendRequestDto createFriendRequest(String username, String friendUsername) throws FriendAlreadyAddedException, CannotAddOwnAccountException, AccountNotFoundException, LoggedInUserNotFoundException {
        return accountRepository.findAccountByUsernameWithFriends(username).map(
                account -> {
                    log.info("Successfully found account with username {}", username);
                    return accountRepository.findAccountByUsernameWithFriends(friendUsername).map(
                            friend -> processFriendRequest(username, friendUsername, account, friend)
                    ).orElseThrow(() -> new AccountNotFoundException("Unable to find account with username " + friendUsername));
                }
        ).orElseThrow(() -> new LoggedInUserNotFoundException("Unable to find account with username " + username));
    }

    private FriendRequestDto processFriendRequest(String username, String friendUsername, Account account, Account friend) {
        log.info("Successfully found account with username {}", friendUsername);
        if (account.getFriends().contains(friend))
            throw new FriendAlreadyAddedException(username + " and " + friendUsername + " are already friends");
        if (account.equals(friend))
            throw new CannotAddOwnAccountException("You cannot become friends with yourself. You'll find people to get along with " + account.getName());

        Optional<FriendRequest> accountToFriendRequest = friendRequestRepository.findFriendRequestByAccountToFriend(friendUsername, username);
        if (accountToFriendRequest.isPresent())
            throw new FriendRequestAlreadyExistsException("You have already sent a friend request to " + friendUsername, Type.ACCOUNT_TO_FRIEND);

        Optional<FriendRequest> friendToAccountRequest = friendRequestRepository.findFriendRequestByFriendToAccount(username, friendUsername);
        if (friendToAccountRequest.isPresent())
            throw new FriendRequestAlreadyExistsException(friendUsername + " has already sent you a friend request", Type.FRIEND_TO_ACCOUNT);

        var friendRequest = new FriendRequest(username + " wants to be your friend", account, friend);

        friendRequest = friendRequestRepository.save(friendRequest);

        return new FriendRequestDto(friendRequest.getId(), friendRequest.getRequestingFriend().getUsername(), friendRequest.getMessage(), friendRequest.getTimestamp());
    }

    @Transactional
    public void acceptRequest(UUID friendRequestId) throws AccountNotFoundException, NotificationNotFoundException {
        var friendRequest = friendRequestRepository.findFriendRequestById(friendRequestId)
                .orElseThrow(() -> new NotificationNotFoundException("unable to find friend request with id " + friendRequestId));
        var account = accountRepository.findByNotificationIdWithFriends(friendRequestId)
                .orElseThrow(() -> new AccountNotFoundException("unable to find account with friend request id " + friendRequestId));
        var friend = friendRequest.getRequestingFriend();
        var friendFriends = accountRepository.findFriendsById(friend.getId());

        account.getFriends().add(friend);
        friendFriends.add(account);
        friend.setFriends(friendFriends);
        account.getNotifications().remove(friendRequest);
        friendRequest.setAccount(null);

        accountRepository.save(account);
        friendRequestRepository.delete(friendRequest);
    }
}
