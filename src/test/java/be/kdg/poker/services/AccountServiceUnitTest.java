package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.AccountDto;
import be.kdg.poker.controllers.dto.AvatarDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Avatar;
import be.kdg.poker.domain.enums.Gender;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AvatarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AccountServiceUnitTest {

    @Autowired
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private AvatarRepository avatarRepository;

    @Test
    void createAccount_ShouldReturnAccount_WhenAccountDoesNotExist() {
        // Arrange
        AccountDto accountDto = new AccountDto(
                UUID.randomUUID(),
                "test@example.com",
                "testuser",
                "Test User",
                LocalDate.of(1990, 1, 1),
                "Test City",
                Gender.MALE,
                List.of(),
                null,
                0,
                0,
                0,
                0,
                0
        );

        Account account = new Account(
                accountDto.email(),
                accountDto.username(),
                accountDto.name(),
                accountDto.age(),
                accountDto.city(),
                accountDto.gender()
        );
        account.setId(UUID.randomUUID());

        Avatar duck = new Avatar();
        duck.setId(UUID.randomUUID());
        duck.setImage("/src/assets/duckpfp.svg");
        duck.setName("duckpfp");
        duck.setPrice(0);

        when(accountRepository.findAccountByEmailAndUsername(accountDto.email(), accountDto.username()))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);
        when(avatarRepository.findAvatarByName("duckpfp")).thenReturn(Optional.of(duck));

        // Act
        AccountDto result = accountService.createAccountIfNotExists(accountDto);

        // Assert
        assertNotNull(result);
        assertEquals(accountDto.email(), result.email());
        assertEquals(accountDto.username(), result.username());
        assertEquals(accountDto.name(), result.name());
        assertEquals(accountDto.age(), result.age());
        assertEquals(accountDto.city(), result.city());
        assertEquals(accountDto.gender(), result.gender());
    }

    @Test
    void createAccount_ShouldReturnNull_WhenAccountAlreadyExists() {
        // Arrange
        AccountDto accountDto = new AccountDto(
                UUID.randomUUID(),
                "test@example.com",
                "testuser",
                "Test User",
                LocalDate.of(1990, 1, 1),
                "Test City",
                Gender.MALE,
                List.of(),
                null,
                0,
                0,
                0,
                0,
                0
        );

        Account existingAccount = new Account();
        existingAccount.setId(UUID.randomUUID());
        existingAccount.setEmail(accountDto.email());
        existingAccount.setUsername(accountDto.username());

        when(accountRepository.findAccountByEmailAndUsername(accountDto.email(), accountDto.username()))
                .thenReturn(Optional.of(existingAccount));

        // Act
        AccountDto result = accountService.createAccountIfNotExists(accountDto);

        // Assert
        assertNull(result);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAvatars_ShouldReturnAvatarDtoList() {
        // Arrange
        String accountEmail = "test@example.com";
        Account account = new Account();
        account.setEmail(accountEmail);
        Avatar avatar1 = new Avatar("Avatar1", "image1", 100);
        Avatar avatar2 = new Avatar("Avatar2", "image2", 200);
        Avatar avatar3 = new Avatar("Avatar3", "image3", 300);
        avatar1.setId(UUID.randomUUID());
        avatar2.setId(UUID.randomUUID());
        avatar3.setId(UUID.randomUUID());

        account.setAvatars(List.of(avatar1, avatar2));

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.of(account));
        when(avatarRepository.findAll()).thenReturn(List.of(avatar1, avatar2, avatar3));

        // Act
        List<AvatarDto> result = accountService.getAvatars();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("Avatar1") && dto.isOwned()));
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("Avatar2") && dto.isOwned()));
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("Avatar3") && !dto.isOwned()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAvatars_ShouldThrowAccountNotFoundException_WhenAccountNotFound() {
        // Arrange
        String accountEmail = "test@example.com";

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.getAvatars());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void buyAvatar_ShouldThrowAccountNotFoundException_WhenAccountNotFound() {
        // Arrange
        String accountEmail = "test@example.com";
        String avatarName = "Avatar1";

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.buyAvatarForLoggedInUser(avatarName));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void buyAvatar_ShouldThrowAccountNotFoundException_WhenAvatarNotFound() {
        // Arrange
        String accountEmail = "test@example.com";
        String avatarName = "Avatar1";
        Account account = new Account();
        account.setEmail(accountEmail);

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.of(account));
        when(avatarRepository.findAvatarByName(avatarName)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.buyAvatarForLoggedInUser(avatarName));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void buyAvatar_ShouldReturnNull_WhenAvatarAlreadyOwned() {
        // Arrange
        String accountEmail = "test@example.com";
        String avatarName = "Avatar1";
        Account account = new Account();
        account.setEmail(accountEmail);
        account.setAvatars(new ArrayList<>());
        Avatar avatar = new Avatar(avatarName, "image1", 100);
        avatar.setId(UUID.randomUUID());
        account.getAvatars().add(avatar);

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.of(account));
        when(avatarRepository.findAvatarByName(avatarName)).thenReturn(Optional.of(avatar));

        // Act
        AvatarDto result = accountService.buyAvatarForLoggedInUser(avatarName);

        // Assert
        assertNull(result);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void buyAvatar_ShouldReturnNull_WhenInsufficientBalance() {
        // Arrange
        String accountEmail = "test@example.com";
        String avatarName = "Avatar1";
        Account account = new Account();
        account.setEmail(accountEmail);
        account.setPokerPoints(50);
        account.setAvatars(new ArrayList<>());
        Avatar avatar = new Avatar(avatarName, "image1", 100);
        avatar.setId(UUID.randomUUID());

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.of(account));
        when(avatarRepository.findAvatarByName(avatarName)).thenReturn(Optional.of(avatar));

        // Act
        AvatarDto result = accountService.buyAvatarForLoggedInUser(avatarName);

        // Assert
        assertNull(result);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void buyAvatar_ShouldReturnAvatarDto_WhenPurchaseSuccessful() {
        // Arrange
        String accountEmail = "test@example.com";
        String avatarName = "Avatar1";
        Account account = new Account();
        account.setEmail(accountEmail);
        account.setPokerPoints(200);
        account.setAvatars(new ArrayList<>());
        Avatar avatar = new Avatar(avatarName, "image1", 100);
        avatar.setId(UUID.randomUUID());

        when(accountRepository.findAccountByEmailWithAvatars(accountEmail)).thenReturn(Optional.of(account));
        when(avatarRepository.findAvatarByName(avatarName)).thenReturn(Optional.of(avatar));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AvatarDto result = accountService.buyAvatarForLoggedInUser(avatarName);

        // Assert
        assertNotNull(result);
        assertEquals(avatarName, result.name());
        assertEquals("image1", result.image());
        assertEquals(100, result.price());
        assertTrue(result.isOwned());
        assertEquals(100, account.getPokerPoints());
        assertTrue(account.getAvatars().contains(avatar));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getPokerPoints_ShouldReturnPokerPoints() {
        // Arrange
        String accountEmail = "test@example.com";
        Account account = new Account();
        account.setEmail(accountEmail);
        account.setPokerPoints(200);
        account.setAvatars(new ArrayList<>());

        // Arrange
        when(accountRepository.findAccountByEmail(accountEmail)).thenReturn(Optional.of(account));

        var result = accountService.getPokerPointsFromLoggedInUser();

        assertEquals(200, result);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getPokerPoints_ShouldThrowAccountNotFoundException_WhenAccountNotFound() {
        // Arrange
        String accountEmail = "test@example.com";
        when(accountRepository.findAccountByEmail(accountEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountService.getPokerPointsFromLoggedInUser());
    }
}