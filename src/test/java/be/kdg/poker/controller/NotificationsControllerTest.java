package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getNotificationGivenValidNotificationIdShouldReturnNotification() throws Exception {
        mockMvc.perform(get("/api/notifications/bd69cd49-d077-4720-9eeb-e7508ea986e2")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bd69cd49-d077-4720-9eeb-e7508ea986e2"))
                .andExpect(jsonPath("$.message").value("Welcome to the game!"))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getNotificationGivenInvalidNotificationIdShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/notifications/bd69cd49-d077-4720-9eeb-e7508ea986e3")
                        .accept("application/json"))
                .andExpect(status().isNotFound())
                .andDo(print());

    }

    @Test
    @WithMockUser(authorities = "user")
    void getGameNotificationGivenValidUsernameShouldReturnGameNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/game/ProPlayer")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("bd69cd49-d077-4720-9eeb-e7508ea986e2"))
                .andExpect(jsonPath("$[0].message").value("Welcome to the game!"))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getGameNotificationGivenInvalidUsernameShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/notifications/game/invalidUsername")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
                .andDo(print());
    }
    @Test
    @WithMockUser(authorities = "user")
    void getAchievementNotificationGivenValidUsernameShouldReturnGameNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/achievement/ProPlayer")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("bd69e349-d077-4720-9eeb-e7508ea986e2"))
                .andExpect(jsonPath("$[0].message").value("You got a new achievement!"))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getAchievementNotificationGivenInvalidUsernameShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/notifications/game/invalidUsername")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getGameInvitesGivenValidUsernameShouldReturnGameInvites() throws Exception {
        mockMvc.perform(get("/api/notifications/invite/PlayerOne")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("09e58459-86af-4cd6-9e42-055f5d652831"))
                .andExpect(jsonPath("$[0].message").value("Robbe has invited you to game"))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getGameInvitesGivenInvalidUsernameShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/notifications/invite/invalidUsername")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
                .andDo(print());
    }

}