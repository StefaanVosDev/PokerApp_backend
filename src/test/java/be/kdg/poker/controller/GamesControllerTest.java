package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.GameDto;
import be.kdg.poker.domain.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class GamesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getGameGivenValidGameIdShouldReturnGameWithPlayersAndOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/games/7fabf988-a888-4dc6-8423-4cd9f620ff00")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7fabf988-a888-4dc6-8423-4cd9f620ff00"))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players.size()").value(greaterThan(0)))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getGameGivenInvalidGameIdShouldReturnBadRequest() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/games/7fabf988-a888-4dc6-8423-4cd9f620ff01")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getGamesShouldReturnListOfGames() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/games")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user", username = "player1@example.com")
    void createGameShouldReturnGame() throws Exception {
        // Arrange
        var mockSettings = new Configuration();
        mockSettings.setBigBlind(10);
        mockSettings.setSmallBlind(5);
        mockSettings.setTimer(false);
        mockSettings.setStartingChips(1000);
        GameDto gameDto = new GameDto(UUID.randomUUID(), null, 5, new ArrayList<>(), new ArrayList<>(), null,"TestGame", mockSettings);
        ObjectMapper objectMapper = new ObjectMapper();
        String gameDtoJson = objectMapper.writeValueAsString(gameDto);

        // Act and Assert
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content(gameDtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestGame"))
                .andExpect(jsonPath("$.maxPlayers").value(5))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = "user", username = "player3@example.com")
    void joinGameShouldReturnOk() throws Exception {
        // Act and Assert
        mockMvc.perform(post("/api/games/6458725a-0945-480d-a006-b37ba0bc53af/join")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user", username = "player2@example.com")
    void isLoggedInUserOnMoveShouldReturnFalse() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/games/3e8c27df-6e15-426b-9c76-5825d61183f7/isOnMove")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("false"))
                .andDo(print());
    }
    @Test
    @WithMockUser(authorities = "user", username="player1@example.com")
    void startGameShouldReturnOk() throws Exception {
        UUID gameId = UUID.fromString("6458725a-0945-480d-a006-b37ba0bc53af");

        mockMvc.perform(put("/api/games/" + gameId + "/status")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getMessagesShouldReturnListOfMessages() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/games/messages?gameId=7fabf988-a888-4dc6-8423-4cd9f620ff00")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(authorities = "user", username = "player1@example.com")
    void addMessageShouldReturnOk() throws Exception {
        // Act and Assert
        mockMvc.perform(post("/api/games/7fabf988-a888-4dc6-8423-4cd9f620ff00/addMessage?message=Hello")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}