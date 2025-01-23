package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Avatar;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AvatarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AvatarServiceUnitTest {

    @Autowired
    private AvatarService avatarService;

    @MockBean
    private AvatarRepository avatarRepository;
    @MockBean
    private AccountRepository accountRepository;

    @Test
    void getAvatarFromLoggedInUser_ShouldReturnValidDto_GivenValidEmail() {
        var mockAccount = new Account();
        mockAccount.setId(UUID.randomUUID());

        var mockAvatar = new Avatar();
        mockAvatar.setImage("/src/assets/bronzeBull.jpg");
        mockAvatar.setId(UUID.randomUUID());

        when(accountRepository.findAccountByEmail(any(String.class))).thenReturn(Optional.of(mockAccount));
        when(avatarRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(mockAvatar));

        var optResult = avatarService.getAvatarFromLoggedInUser("bleh");

        assertTrue(optResult.isPresent());

        var result = optResult.get();

        assertEquals(result.image(), mockAvatar.getImage());
        assertEquals(result.username(), mockAccount.getUsername());
        assertEquals(result.id(), mockAvatar.getId());
    }

    @Test
    void getAvatarFromLoggedInUser_ShouldReturnEmptyOptional_GivenValidEmailOfEmptyAccount() {
        var mockAccount = new Account();
        mockAccount.setId(UUID.randomUUID());

        when(accountRepository.findAccountByEmail(any(String.class))).thenReturn(Optional.of(mockAccount));
        when(avatarRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.empty());

        var optResult = avatarService.getAvatarFromLoggedInUser("bleh");

        assertTrue(optResult.isEmpty());
    }

    @Test
    void getAvatarFromLoggedInUser_ShouldThrowException_GivenInvalidEmail() {
        when(accountRepository.findAccountByEmail(any(String.class))).thenReturn(Optional.empty());

        Executable executable = () -> avatarService.getAvatarFromLoggedInUser("bleh");

        assertThrows(AccountNotFoundException.class, executable);
    }
}