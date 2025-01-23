package be.kdg.poker.services;

import be.kdg.poker.controllers.dto.AccountDto;
import be.kdg.poker.controllers.dto.AvatarDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.Avatar;
import be.kdg.poker.domain.events.user.UserActivityEvent;
import be.kdg.poker.domain.events.user.UserProfileEvent;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.AvatarNotFoundException;
import be.kdg.poker.exceptions.AvatarNotOwnedException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.AchievementRepository;
import be.kdg.poker.repositories.AvatarRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AvatarRepository avatarRepository;
    private final EventService eventService;
    private final AchievementRepository achievementRepository;

    public AccountService(AccountRepository accountRepository, AvatarRepository avatarRepository, EventService eventService, AchievementRepository achievementRepository) {
        this.accountRepository = accountRepository;
        this.avatarRepository = avatarRepository;
        this.eventService = eventService;
        this.achievementRepository = achievementRepository;
    }

    @Transactional
    public AccountDto createAccountIfNotExists(AccountDto accountDto) {
        log.info("checking if the account with email {} and username {} exists", accountDto.email(), accountDto.username());
        Optional<Account> doesAccountExist = accountRepository.findAccountByEmailAndUsername(accountDto.email(), accountDto.username());

        if (doesAccountExist.isPresent()) {
            log.info("Account with email {} and username {} already exists", accountDto.email(), accountDto.username());
            UserActivityEvent loginEvent = new UserActivityEvent(
                    doesAccountExist.get().getId().toString(),
                    "login",
                    LocalDateTime.now()
            );
            eventService.sendUserActivityEvent(loginEvent);
            return null;
        }

        log.info("Creating a new account with email {} and username {}", accountDto.email(), accountDto.username());
        Account account = new Account(accountDto.email(), accountDto.username(), accountDto.name(), accountDto.age(), accountDto.city(), accountDto.gender());

        var duck = avatarRepository.findAvatarByName("duckpfp").orElseThrow(() -> new AvatarNotFoundException("duck profile pic cannot be found"));

        account.getAvatars().add(duck);
        account.setActiveAvatar(duck);

        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created account for username: {}, ID: {}", accountDto.username(), savedAccount.getId());

        int age = calculateAge(savedAccount.getAge());
        String genderString;
        if (savedAccount.getGender() == null) genderString = "";
        else genderString = savedAccount.getGender().toString();
        // UserProfileEvent versturen
        UserProfileEvent userProfileEvent = new UserProfileEvent(
                savedAccount.getId().toString(),
                age,
                savedAccount.getCity(),
                genderString,
                LocalDateTime.now()
        );
        eventService.sendUserProfileEvent(userProfileEvent);
        log.info("UserProfileEvent sent for userId: {}, age: {}", savedAccount.getId(), age);

        return mapToDto(savedAccount);
    }

    public String getLoggedInUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            return ((Jwt) principal).getClaimAsString("email");
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    public Optional<AccountDto> findByUsernameWithOwnedAvatars(String username) {
        return accountRepository.findByUsernameWithAvatars(username)
                .map(account -> {
                    var avatar = avatarRepository.findByAccountId(account.getId())
                            .orElseThrow(() -> new AvatarNotFoundException("Account with username " + username + " does not have an active avatar"));
                    account.setActiveAvatar(avatar);
                    var accountWithCounterAchievements = accountRepository.findByAccountIdWithCounters(account.getId());
                    account.setCountersAchievements(accountWithCounterAchievements.get().getCountersAchievements());
                    return account;
                })
                .map(this::mapToDto);
    }

    public List<AvatarDto> getAvatars() {
        String accountEmail = getLoggedInUserEmail();
        List<Avatar> avatars = avatarRepository.findAll();
        Account account = accountRepository.findAccountByEmailWithAvatars(accountEmail)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + accountEmail + " not found"));
        List<AvatarDto> avatarDtoList = new ArrayList<>();
        for (Avatar avatar : avatars) {
            boolean isOwned = account.getAvatars().contains(avatar);
            avatarDtoList.add(new AvatarDto(avatar.getName(), avatar.getImage(), avatar.getPrice(), isOwned));
        }
        return avatarDtoList;
    }

    @Transactional
    public AvatarDto buyAvatarForLoggedInUser(String avatarName) {
        String accountEmail = getLoggedInUserEmail();
        Account account = accountRepository.findAccountByEmailWithAvatars(accountEmail)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + accountEmail + " not found"));
        Avatar avatar = avatarRepository.findAvatarByName(avatarName)
                .orElseThrow(() -> new AccountNotFoundException("Avatar with name " + avatarName + " not found"));

        if (account.getAvatars().contains(avatar)) {
            log.info("Account with email {} already owns avatar with name {}", accountEmail, avatarName);
            return null;
        }
        if (account.getPokerPoints() < avatar.getPrice()) {
            log.error("Account with email {} does not have enough balance to buy avatar with name {}", accountEmail, avatarName);
            return null;
        }
        account.getAvatars().add(avatar);
        account.setPokerPoints(account.getPokerPoints() - avatar.getPrice());
        accountRepository.save(account);
        log.info("Account with email {} successfully bought avatar with name {}", accountEmail, avatarName);
        return new AvatarDto(avatar.getName(), avatar.getImage(), avatar.getPrice(), true);
    }

    public int getPokerPointsFromLoggedInUser() {
        String accountEmail = getLoggedInUserEmail();
        Account account = accountRepository.findAccountByEmail(accountEmail)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + accountEmail + " not found"));
        return account.getPokerPoints();
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        if (birthDate.isAfter(LocalDate.now())) return -1;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public AccountDto mapToDto(Account account) {
        return new AccountDto(account.getId(),account.getEmail(), account.getUsername(), account.getName(), account.getAge(), account.getCity(), account.getGender(), account.getAvatars(), account.getActiveAvatar(),account.getCountersAchievements().getOrDefault("wins",0), account.getCountersAchievements().getOrDefault("playedGames",0), account.getCountersAchievements().getOrDefault("royal flushes",0), account.getCountersAchievements().getOrDefault("flushes",0), account.getCountersAchievements().getOrDefault("straights",0));
    }

    public void selectAvatar(String username, UUID avatarId) throws AvatarNotOwnedException, AvatarNotFoundException, AccountNotFoundException {
        accountRepository.findByUsernameWithAvatars(username).ifPresentOrElse(
                account -> {
                    log.info("account with username {} found", username);
                    avatarRepository.findById(avatarId).ifPresentOrElse(
                            avatar -> {
                                if (!account.getAvatars().contains(avatar)) {
                                    throw new AvatarNotOwnedException("Account with username " + username + " does not own avatar with id " + avatarId);
                                }
                                account.setActiveAvatar(avatar);
                                accountRepository.save(account);
                            },
                            () -> {
                                log.error("avatar with id {} does not exist", avatarId);
                                throw new AvatarNotFoundException("Avatar with id " + avatarId + " does not exist");
                            }
                    );
                },
                () -> {
                    log.error("account with username {} does not exist", username);
                    throw new AccountNotFoundException("Account with username " + username + " does not exist");
                }
        );
    }
}
