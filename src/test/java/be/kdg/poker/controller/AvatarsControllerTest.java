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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AvatarsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user", value = "robbe@student.kdg.be")
    void getAvatarFromLoggedInUser_ShouldReturnOk_WhenValidUserIsLoggedIn() throws Exception {
        mockMvc.perform(get("/api/avatars/loggedIn")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("https://storage.googleapis.com/poker_stacks/avatars/bronzeBull.svg"));
    }

    @Test
    @WithMockUser(authorities = "user", username = "player1@example.com")
    void getAvatarsGivenValidAccountDtoShouldReturnAccountAndOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/avatars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user", username = "player1@example.com")
    void buyAvatarGivenValidAccountDtoShouldReturnAccountAndOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(post("/api/avatars/buy")
                        .param("name", "Silver Shark")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        ;
    }

}