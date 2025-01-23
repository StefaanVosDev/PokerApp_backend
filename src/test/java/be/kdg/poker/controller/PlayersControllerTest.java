package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PlayersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getWinnerGivenValidWinnerIdShouldReturnWinner() throws Exception {
        mockMvc.perform(get("/api/players/winner?winnerId=fbe9bdbf-4329-4fea-9bed-0449d5677804")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @WithMockUser(authorities = "user")
    void getPlayerOnMove_ShouldReturnPlayerDto_WhenPlayerIsOnMove() throws Exception {
        // Arrange
        UUID gameId = UUID.fromString("df6b6682-0bdc-4c10-9471-d6c752963b1c");

        // Act & Assert
        mockMvc.perform(get("/api/players/" + gameId + "/playerOnMove")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @WithMockUser(authorities = "user")
    void getPlayersOnMove_ShouldReturnPlayerGameDto_WhenPlayerIsOnMove() throws Exception {

        // Act & Assert
        mockMvc.perform(get("/api/players/playersOnMove")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
