package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.FriendsListDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Avatar;
import be.kdg.poker.domain.enums.Gender;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.CannotAddOwnAccountException;
import be.kdg.poker.exceptions.LoggedInUserNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FriendServiceUnitTest {

    @Autowired
    private FriendService friendService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @Test
    void getFriendsGivenValidUsername_ShouldReturnAccount() {
        // Arrange
        String validUsername = "robbe";


        Account testAccount = new Account();
        testAccount.setUsername(validUsername);
        testAccount.setEmail("robbe@example.com");
        testAccount.setName("Robbe");
        testAccount.setCity("Test City");
        testAccount.setGender(Gender.MALE);


        Account friend = new Account();
        friend.setUsername("friend1");
        friend.setEmail("friend1@example.com");
        friend.setName("Friend One");
        friend.setCity("Friend City");
        friend.setGender(Gender.MALE);

        Avatar avatar = new Avatar();
        avatar.setImage("avatar1.jpg");

        friend.setActiveAvatar(avatar);

        testAccount.setFriends(List.of(friend));

        when(accountRepository.findAccountByUsername(validUsername)).thenReturn(Optional.of(testAccount));
        accountRepository.save(friend);
        accountRepository.save(testAccount);


        // Act
        List<FriendsListDto> friendsList = friendService.getFriends(validUsername);

        // Assert
        assertNotNull(friendsList, "Friends list should not be null");
        assertFalse(friendsList.isEmpty(), "Friends list should not be empty");
        assertEquals(1, friendsList.size(), "Friends list should contain one friend");
        assertEquals("friend1", friendsList.get(0).username(), "Friend's username should match");
    }

    @Test
    void getFriendsGivenInvalidUsername_ShouldReturnAccountNotFoundException() {
        // Arrange
        String invalidUsername = "nonexistent";
        when(accountRepository.findAccountByUsername(invalidUsername)).thenReturn(Optional.empty());

        // Act
        assertThrows(AccountNotFoundException.class, () -> friendService.getFriends(invalidUsername));
    }

    @Test
    void getFriendsGivenNoFriends_ShouldReturnEmptyList() {
        // Arrange
        String validUsername = "nofriends";

        Account mockAccount = new Account();
        mockAccount.setUsername(validUsername);
        mockAccount.setFriends(new ArrayList<>()); // No friends

        when(accountRepository.findAccountByUsername(validUsername)).thenReturn(Optional.of(mockAccount));

        // Act
        List<FriendsListDto> friendsList = friendService.getFriends(validUsername);

        // Assert
        assertNotNull(friendsList);
        assertTrue(friendsList.isEmpty());
    }

    // there is only a happy path, because it's physically impossible to delete a friend that doesn't exist in the friends list on in the front end
    // only friends that are in ur friends list are displayed there.
    @Test
    void deleteFriend_ShouldRemoveFriend_WhenFriendExists() {
        // Arrange
        String username = "robbe";
        String friendUsername = "afi";

        Account account = new Account();
        account.setUsername(username);

        Account friend = new Account();
        friend.setUsername(friendUsername);

        ArrayList<Account> friends = new ArrayList<>();
        friends.add(friend);
        account.setFriends(friends);

        ArrayList<Account> friendFriends = new ArrayList<>();
        friendFriends.add(account);
        friend.setFriends(friendFriends);

        when(accountRepository.findAccountByUsername(username)).thenReturn(Optional.of(account));
        when(accountRepository.findAccountByUsername(friendUsername)).thenReturn(Optional.of(friend));

        // Act
        friendService.deleteFriend(username, friendUsername);

        // Assert
        assertTrue(account.getFriends().stream().noneMatch(f -> f.getUsername().equals(friendUsername)));
    }

    // happy path already tested in controller. would have no added value here.
    @Test
    void createFriendRequest_ShouldThrowAccountNotFoundException_GivenInvalidUsername() {
        var mockAccount = new Account();
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setLevel(10);
        mockAccount.setAge(LocalDate.of(2004, 9, 3));
        mockAccount.setName("robbe van osselaer");
        mockAccount.setEmail("robbe@hotmail.com");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setCity("Duffel");
        mockAccount.setUsername("robbe");
        mockAccount.setPokerPoints(1000);

        when(accountRepository.findAccountByUsernameWithFriends("robbe")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.findAccountByUsernameWithFriends("suuuuiiii")).thenReturn(Optional.empty());

        Executable executable = () -> friendService.createFriendRequest("suuuuiiii", "robbe");
        Executable friendExecutable = () -> friendService.createFriendRequest("robbe", "suuuuiiii");

        var errorMessage = "Unable to find account with username suuuuiiii";
        assertThrows(LoggedInUserNotFoundException.class, executable);

        try {
            executable.execute();
        } catch (Throwable e) {
            assertEquals(errorMessage, e.getMessage());
        }

        assertThrows(AccountNotFoundException.class, friendExecutable);

        try {
            friendExecutable.execute();
        } catch (Throwable e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    void createFriendRequest_ShouldThrowFriendAlreadyAddedException_GivenBefriendedUsernames() {
        // Mock account 1
        var mockAccount = new Account();
        mockAccount.setId(UUID.randomUUID());
        mockAccount.setLevel(10);
        mockAccount.setAge(LocalDate.of(2004, 9, 3));
        mockAccount.setName("robbe van osselaer");
        mockAccount.setEmail("robbe@hotmail.com");
        mockAccount.setGender(Gender.MALE);
        mockAccount.setCity("Duffel");
        mockAccount.setUsername("robbe");
        mockAccount.setPokerPoints(1000);

        // Mock account 2
        var mockAccount2 = new Account();
        mockAccount2.setId(UUID.randomUUID());
        mockAccount2.setLevel(10);
        mockAccount2.setAge(LocalDate.of(1985, 2, 5));
        mockAccount2.setName("Cristiano Ronaldo");
        mockAccount2.setEmail("ronaldo@moneyman.sa");
        mockAccount2.setGender(Gender.MALE);
        mockAccount2.setCity("Ryadh");
        mockAccount2.setUsername("suuuuiiii");
        mockAccount2.setPokerPoints(1000000);

        // Set the friends list
        mockAccount.setFriends(List.of(mockAccount2));
        mockAccount2.setFriends(List.of(mockAccount));

        // Mock repository behavior
        when(accountRepository.findAccountByUsernameWithFriends("robbe")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.findAccountByUsernameWithFriends("suuuuiiii")).thenReturn(Optional.of(mockAccount2));

        // Executable to test self-friendship
        Executable executable = () -> friendService.createFriendRequest("robbe", "robbe");

        var errorMessage = "You cannot become friends with yourself. You'll find people to get along with robbe van osselaer";
        var exception = assertThrows(CannotAddOwnAccountException.class, executable);

        assertEquals(errorMessage, exception.getMessage());
    }
}