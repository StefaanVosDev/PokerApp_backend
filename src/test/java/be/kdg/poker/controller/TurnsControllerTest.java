package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TurnsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "robbe@student.kdg.be", authorities = "user")
    void checkAndMove_ShouldReturnOk_WhenSuccessful() throws Exception {

        mockMvc.perform(put("/api/turns/42fb6a81-96ef-46d0-95d9-ff4d251c1530/checkAndMove")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .param("roundId", "668bbee6-eeca-4123-a5d5-a3217f96c26a")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void checkAndMove_ShouldReturnNotFound_WhenTurnNotFound() throws Exception {
        UUID turnId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();


        mockMvc.perform(put("/api/turns/" + turnId + "/checkAndMove")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .param("playerId", playerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void foldAndMove_ShouldReturnOk_WhenSuccessful() throws Exception {

        mockMvc.perform(put("/api/turns/42fb6a81-96ef-46d0-95d9-ff4d251c1530/foldAndMove")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .param("roundId", "668bbee6-eeca-4123-a5d5-a3217f96c26a")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void foldAndMove_ShouldReturnNotFound_WhenTurnNotFound() throws Exception {
        UUID turnId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();

        mockMvc.perform(put("/api/turns/" + turnId + "/foldAndMove")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "robbe@student.kdg.be", authorities = "user")
    void callAndMove_ShouldReturnOk_WhenSuccessful() throws Exception {
        int amount = 100;

        mockMvc.perform(put("/api/turns/42fb6a81-96ef-46d0-95d9-ff4d251c1530/callAndMove")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .param("roundId", "668bbee6-eeca-4123-a5d5-a3217f96c26a")
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void callAndMove_ShouldReturnNotFound_WhenTurnNotFound() throws Exception {
        UUID turnId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        int amount = 100;

        mockMvc.perform(put("/api/turns/" + turnId + "/callAndMove")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "robbe@student.kdg.be", authorities = "user")
    void raiseAndMove_ShouldReturnOk_WhenSuccessful() throws Exception {
        int amount = 100;

        mockMvc.perform(put("/api/turns/42fb6a81-96ef-46d0-95d9-ff4d251c1530/raiseAndMove")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .param("roundId", "668bbee6-eeca-4123-a5d5-a3217f96c26a")
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void raiseAndMove_ShouldReturnNotFound_WhenTurnNotFound() throws Exception {
        UUID turnId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        int amount = 100;

        mockMvc.perform(put("/api/turns/" + turnId + "/raiseAndMove")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "robbe@student.kdg.be", authorities = "user")
    void allinAndMove_ShouldReturnOk_WhenSuccessful() throws Exception {

        mockMvc.perform(put("/api/turns/8b6fe2a7-0c4e-4f8c-86dc-8283b618a58d/allinAndMove")
                        .param("gameId", "df6b6682-0bdc-4c10-9471-d6c752963b1c")
                        .param("roundId", "33c7aef3-c4bc-4235-a4b4-74b305d84c33")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void allinAndMove_ShouldReturnNotFound_WhenTurnNotFound() throws Exception {
        UUID turnId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();


        mockMvc.perform(put("/api/turns/" + turnId + "/allinAndMove")
                        .param("gameId", gameId.toString())
                        .param("roundId", roundId.toString())
                        .param("playerId", playerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    @Order(1)
    void getCurrentTurn_ShouldReturnOk_WhenTurnIsOnMove() throws Exception {

        mockMvc.perform(get("/api/turns/current")
                        .param("gameId", "3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("42fb6a81-96ef-46d0-95d9-ff4d251c1530"));
    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentTurn_ShouldReturnOk_WhenRoundIsFinished() throws Exception {

        mockMvc.perform(get("/api/turns/current")
                        .param("gameId", "d687dbce-4435-490f-b92c-6741e363eace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentTurn_ShouldReturnNotFound_WhenGameNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();

        mockMvc.perform(get("/api/turns/current")
                        .param("gameId", gameId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentTurn_ShouldReturnNotFound_WhenRoundNotFound() throws Exception {

        mockMvc.perform(get("/api/turns/current")
                        .param("gameId", "7fabf988-a888-4dc6-8423-4cd9f620ff01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getCurrentTurn_ShouldReturnNotFound_WhenNoTurns() throws Exception {
        mockMvc.perform(get("/api/turns/current")
                        .param("gameId", "da06fffb-4855-4776-8a2c-62e39c3ebdd9")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getTurnsByRoundId_ShouldReturnOk_WhenTurnsExist() throws Exception {

        mockMvc.perform(get("/api/turns/round")
                        .param("roundId", "0e6ea81f-5527-4aef-98cb-2afb233fd8af")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].player.id").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getTurnsByRoundId_ShouldReturnEmptyList_WhenNoTurnsExist() throws Exception {
        mockMvc.perform(get("/api/turns/round")
                        .param("roundId", "cccf7b01-4f8e-4c18-ae6b-3af5c663dfb6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}