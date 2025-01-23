package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.AvatarDto;
import be.kdg.poker.controllers.dto.AvatarWithUsernameDto;
import be.kdg.poker.services.AccountService;
import be.kdg.poker.services.AvatarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/avatars")
public class AvatarsController {
    private final AvatarService avatarService;
    private final AccountService accountService;

    public AvatarsController(AvatarService avatarService, AccountService accountService) {
        this.avatarService = avatarService;
        this.accountService = accountService;
    }

    @GetMapping("/loggedIn")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<AvatarWithUsernameDto> getAvatarFromLoggedInUser() {
        log.info("successfully received request to get avatar from currently logged in user");
        return avatarService.getAvatarFromLoggedInUser(accountService.getLoggedInUserEmail())
                .map(avatarDto -> {
                    log.info("Successfully found avatar from logged in user");
                    return ResponseEntity.ok(avatarDto);
                })
                .orElseGet(() -> {
                    log.error("unable to find avatar from logged in user");
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<AvatarDto>> getAvatars() {
        log.info("Received request to get avatars");
        return ResponseEntity.ok(accountService.getAvatars());
    }

    @PostMapping("/buy")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<AvatarDto> buyAvatar(@RequestParam String name) {
        log.info("Received request to buy avatar with name {}", name);
        return ResponseEntity.ok(accountService.buyAvatarForLoggedInUser(name));
    }
}
