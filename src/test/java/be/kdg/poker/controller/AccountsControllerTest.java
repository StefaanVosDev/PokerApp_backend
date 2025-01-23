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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @WithMockUser(authorities = "user")
    void createAccountGivenValidAccountDtoShouldReturnAccountAndOkStatus() throws Exception {
        // Act and Assert
        var jsonBody = """
                {
                    "email": "test@example.com",
                    "username": "testuser",
                    "name": "Test User",
                    "age": "1990-01-01",
                    "city": "Test City",
                    "gender": "MALE"
                }
                """;

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void createAccount_GivenInvalidAccountDto_ShouldReturnBadRequestStatus() throws Exception {
        // Act and Assert
        var jsonBody = """
                {
                    "email": "test@example.com",
                    "username": "testuser",
                    "name": "Test User",
                    "age": "01 Jan 1990",
                    "city": "Test City",
                    "gender": "MALE"
                }
                """;

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(authorities = "user")
    void getAccount_ShouldReturnOk_GivenValidId() throws Exception {
        mockMvc.perform(get("/api/accounts/robbe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("robbe@student.kdg.be"))
                .andExpect(jsonPath("$.username").value("robbe"))
                .andExpect(jsonPath("$.age").value("2004-09-03"))
                .andExpect(jsonPath("$.city").value("Duffel"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.ownedAvatars.size()").value(2));
    }

    @Test
    @WithMockUser(authorities = "user")
    void getAccount_ShouldReturnNotFound_GivenInvalidId() throws Exception {
        mockMvc.perform(get("/api/accounts/" + UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "user", username = "player1@example.com")
    void getPokerPointsGivenValidAccountShouldReturnPokerPointsAndOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/accounts/poker-points")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150));
    }

    @Test
    @WithMockUser(authorities = "user")
    void selectAvatar_ShouldReturnOk_GivenValidUsernameAndAvatarId() throws Exception {
        mockMvc.perform(put("/api/accounts/Johnknee Rovo2.0/active-avatar").
                        param("avatarId", "91e4592f-4bb1-438c-b9f9-11c756fac0d4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user")
    void selectAvatar_ShouldReturnInternalServerError_GivenInvalidUsername() throws Exception {
        mockMvc.perform(put("/api/accounts/ebbor/active-avatar").
                        param("avatarId", "91e4592f-4bb1-438c-b9f9-11c756fac0d4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Account with username ebbor does not exist"));
    }

    @Test
    @WithMockUser(authorities = "user")
    void selectAvatar_ShouldReturnBadRequest_GivenValidUsernameAndAvatarIdButAvatarIsNotOwned() throws Exception {
        mockMvc.perform(put("/api/accounts/robbe/active-avatar").
                        param("avatarId", "72f119be-f1a2-44ae-9831-7d2227a725b6")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Account with username robbe does not own avatar with id 72f119be-f1a2-44ae-9831-7d2227a725b6"));
    }

    @Test
    @WithMockUser(authorities = "user")
    void selectAvatar_ShouldReturnNotFound_GivenInvalidAvatarId() throws Exception {
        var avatarId = UUID.randomUUID().toString();

        mockMvc.perform(put("/api/accounts/robbe/active-avatar").
                        param("avatarId", avatarId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Avatar with id " + avatarId + " does not exist"));
    }
}