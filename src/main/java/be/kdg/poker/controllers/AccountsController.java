package be.kdg.poker.controllers;


import be.kdg.poker.controllers.dto.AccountDto;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.exceptions.AvatarNotFoundException;
import be.kdg.poker.exceptions.AvatarNotOwnedException;
import be.kdg.poker.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    private final AccountService accountService;

    public AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }


    @PostMapping()
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<AccountDto> createAccountIfNotExists(@RequestBody AccountDto accountDto) {
        log.info("Received request to create a new account");
        AccountDto account = accountService.createAccountIfNotExists(accountDto);
        return ResponseEntity.ok(account);
    }


    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String username) {
        return accountService.findByUsernameWithOwnedAvatars(username)
                .map(account -> {
                            log.info("successfully found account with username {}", username);
                            return ResponseEntity.ok(account);
                        }
                ).orElseGet(() -> {
                    log.error("unable to find account with username {}", username);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/poker-points")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Integer> getPokerPoints() {
        log.info("Received request to get account");
        int pokerPoints = accountService.getPokerPointsFromLoggedInUser();
        return ResponseEntity.ok(pokerPoints);
    }

    @PutMapping("{username}/active-avatar")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<String> selectAvatar(@PathVariable String username, @RequestParam UUID avatarId) {
        log.info("successfully received request to select avatar");
        var generalErrorMessage = "error selecting avatar: ";
        try {
            accountService.selectAvatar(username, avatarId);
            log.info("successfully updated account/selected avatar");
            return ResponseEntity.ok().build();
        } catch (AccountNotFoundException e) {
            log.error(generalErrorMessage + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (AvatarNotOwnedException e) {
            log.error(generalErrorMessage + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AvatarNotFoundException e) {
            log.error(generalErrorMessage + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
