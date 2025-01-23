
package be.kdg.poker.services;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.AccountDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.enums.Gender;
import be.kdg.poker.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AccountServiceTest {

    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

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
                null,
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
        accountDto.gender());
        account.setId(UUID.randomUUID());

        when(accountRepository.findAccountByEmailAndUsername(accountDto.email(), accountDto.username())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

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
                null,
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
}