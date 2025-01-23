package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getAchievements_ShouldReturnOkAndAchievementsList_WhenUserHasAuthority() throws Exception {
        mockMvc.perform(get("/api/achievements")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("5 Wins"))
                .andExpect(jsonPath("$[1].name").value("10 Wins"));
    }

    @Test
    void getAchievements_ShouldReturnForbidden_WhenUserIsNotAuthorized() throws Exception {
        mockMvc.perform(get("/api/achievements")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getAchievementsByAccountId_ShouldReturnOkAndAchievementsList_WhenValidAccountIdProvided() throws Exception {
        UUID accountId = UUID.fromString("8a6047e7-89a9-4882-8336-438764a00b35");
        mockMvc.perform(get("/api/achievements/" + accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("5 Wins"));
    }
}
