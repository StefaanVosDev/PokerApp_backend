package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.AvatarWithUsernameDto;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AvatarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final AccountRepository accountRepository;

    public AvatarService(AvatarRepository avatarRepository, AccountRepository accountRepository) {
        this.avatarRepository = avatarRepository;
        this.accountRepository = accountRepository;
    }

    public Optional<AvatarWithUsernameDto> getAvatarFromLoggedInUser(String loggedInUserEmail) {
        var account = accountRepository.findAccountByEmail(loggedInUserEmail).orElseThrow(() -> {
            log.error("the logged in user with email {} does not have an account", loggedInUserEmail);
            return new AccountNotFoundException("the logged in user with email "+ loggedInUserEmail + " does not have an account");
        });
        return avatarRepository.findByAccountId(account.getId()).map(avatar -> new AvatarWithUsernameDto(avatar.getId(), avatar.getImage(), account.getUsername()));
    }
}
