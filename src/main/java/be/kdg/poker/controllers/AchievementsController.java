package be.kdg.poker.controllers;

import be.kdg.poker.controllers.dto.AchievementDto;
import be.kdg.poker.services.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/achievements")
public class AchievementsController {
    private final AchievementService achievementService;

    public AchievementsController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<AchievementDto>> getAchievements() {
        log.info("Received request to get all achievements");
        List<AchievementDto> achievements = achievementService.getAchievements();
        log.info("Successfully retrieved all the achievements");
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<AchievementDto>> getAchievementsByAccountId(@PathVariable UUID accountId) {
        List<AchievementDto> achievements = achievementService.getAchievementsByAccountId(accountId);
        return ResponseEntity.ok(achievements);
    }
}
