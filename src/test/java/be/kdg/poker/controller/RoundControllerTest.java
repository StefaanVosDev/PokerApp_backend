package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.domain.enums.Suit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class RoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getCommunityCards_ShouldReturnCommunityCards_WhenGameIdIsValid() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/rounds/communityCards")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].suit").value(Suit.SPADES.toString()))
                .andExpect(jsonPath("$[0].rank").value(2));
    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentRound_ShouldReturnCurrentRound_WhenGameIdIsValid() throws Exception {
        mockMvc.perform(get("/api/rounds/current")
                        .param("gameId", "d687dbce-4435-490f-b92c-6741e363eace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c01cc034-0d9f-4319-8091-0672b528f942"));

    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentRound_ShouldReturnNotFound_WhenGameIdIsInvalid() throws Exception {
        UUID gameId = UUID.randomUUID();

        mockMvc.perform(get("/api/rounds/current")
                        .param("gameId", gameId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void dividePot_ShouldReturnOk_GivenValidIdOfFinishedRound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rounds/c01cc034-0d9f-4319-8091-0672b528f942/dividePot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0]").value("a67a46f6-550f-4aaf-b2ee-adb0446db889"));
    }

    @Test
    @WithMockUser(authorities = "user")
    void dividePot_ShouldReturnOkWithEveryoneGivenBackTheirMoney_GivenValidIdOfFinishedRoundWithWinningHandInCommunityCards() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rounds/33c7aef3-c4bc-4235-a4b4-74b305d84c33/dividePot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0]").value("bbc1734a-f56c-4392-a83a-b7587787f727"))
                .andExpect(jsonPath("$[1]").value("be264183-a2b9-4b90-8fb4-84840a21d011"));

    }

    @Test
    @WithMockUser(authorities = "user")
    void dividePot_ShouldReturnNotFound_GivenInvalidId() throws Exception {
        UUID roundId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rounds/" + roundId + "/dividePot")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void createNewRoundIfFinished_ShouldReturnNotFound_GivenRoundThatIsNotFinished() throws Exception {
        UUID gameId = UUID.fromString("3e8c27df-6e15-426b-9c76-5825d61183f7");
        UUID roundId = UUID.fromString("668bbee6-eeca-4123-a5d5-a3217f96c26a");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rounds/createNewRoundIfFinished")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void createNewRoundIfFinished_ShouldReturnOk_GivenValidGameAndRoundId() throws Exception {
        UUID gameId = UUID.fromString("30616ea3-7e32-40c6-91d5-2829a2ad615b");
        UUID roundId = UUID.fromString("61870bbf-7e8a-4157-b524-c8d4afb5aee4");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rounds/createNewRoundIfFinished")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void createNewRoundIfFinished_ShouldReturnNoContent_GivenGameWithOnlyOnePlayer() throws Exception {
        UUID gameId = UUID.fromString("da06fffb-4855-4776-8a2c-62e39c3ebdd9");
        UUID roundId = UUID.fromString("61870bbf-7e8a-4157-b524-c8d4afb5aee4");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/rounds/createNewRoundIfFinished")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}