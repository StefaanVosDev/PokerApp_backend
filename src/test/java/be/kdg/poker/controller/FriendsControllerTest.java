package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.exceptions.AccountNotFoundException;
import jakarta.servlet.ServletException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @WithMockUser(authorities = "user")
    void getFriendsGivenValidUsernameReturnsOkStatus() throws Exception {

        mockMvc.perform(get("/api/friends/PlayerOne")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("ProPlayer"))
                .andExpect(jsonPath("$[0].image").value("https://storage.googleapis.com/poker_stacks/avatars/silverShark.svg"));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "user")
    void getFriendsGivenInValidUsernameReturnsNotFoundStatus() {
        ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/friends/nofrinds")
                        .accept(MediaType.APPLICATION_JSON))
        );

        assertEquals(AccountNotFoundException.class, exception.getCause().getClass());
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "user")
    void getFriendsGivenValidUsernameAndNoFriendsReturnsNoContent() throws Exception {
        mockMvc.perform(get("/api/friends/nofriends")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "user")
    void requestFriendGivenValidUsernameReturnsOkStatus() throws Exception {
        mockMvc.perform(post("/api/friends/afi/robbe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("afi wants to be your friend"));
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "user")
    void requestFriendGivenUsernameOfAlreadyReceivedFriendRequestGivesConflictException() throws Exception {

        mockMvc.perform(post("/api/friends/robbe/PlayerOne")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "user")
    void requestFriendGivenUsernameOfAlreadySendFriendRequestGivesToManyRequestsException() throws Exception {

        mockMvc.perform(post("/api/friends/PlayerOne/robbe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "user")
    void acceptRequest_ShouldReturnOk_GivenValidFriendRequestId() throws Exception {
        mockMvc.perform(put("/api/friends/accept")
                        .param("friendRequestId", "68c3fbe8-0d5c-4440-940c-24349f943462")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "user")
    void acceptRequest_ShouldReturnNotFound_GivenInvalidFriendRequestId() throws Exception {
        mockMvc.perform(put("/api/friends/accept")
                        .param("friendRequestId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "user")
    void requestFriendGivenUsernameOfAlreadyAddedFriendGivesBadRequest() throws Exception {

        mockMvc.perform(post("/api/friends/ProPlayer/LuckyPlayer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @WithMockUser(authorities = "user")
    void requestFriendGivenUsernameOfOwnAccountGivesBadRequest() throws Exception {

        mockMvc.perform(post("/api/friends/robbe/robbe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnavailableForLegalReasons());
    }

    @Test
    @Order(11)
    @WithMockUser(authorities = "user")
    void requestFriendGivenUsernameOfAccountThatDoesntExistGivesNotFoundException() throws Exception {

        mockMvc.perform(post("/api/friends/robbe/afiiiiii")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Only a happy path, because the unhappy path doesn't exist, the delete friend api endpoint only gets triggered when the user deletes a friend that is displayed
    // in the friendslist, so it's physically impossible to delete a friend that doesn't exist in that list.
    @Test
    @Order(12)
    @WithMockUser(authorities = "user")
    void deleteFriendGivenValidUsernameReturnsOkStatus() throws Exception {

        mockMvc.perform(delete("/api/friends/robbe/afi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    @WithMockUser(authorities = "user")
    void declineRequest_ShouldReturnOk_GivenValidFriendRequestId() throws Exception {
        mockMvc.perform(delete("/api/friends/decline")
                        .param("friendRequestId", "14e77d07-38a9-4819-940e-9627495ebef0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    @WithMockUser(authorities = "user")
    void declineRequest_ShouldReturnNotFound_GivenInvalidFriendRequestId() throws Exception {
        mockMvc.perform(delete("/api/friends/decline")
                        .param("friendRequestId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    @WithMockUser(authorities = "user")
    void getFriendRequestsFromAccountUsernameReturnsOkStatus() throws Exception {

        mockMvc.perform(get("/api/friends/requests/robbe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].message").value("afi wants to be your friend"))
                .andExpect(jsonPath("$[0].requestingFriendUsername").value("afi"));
    }

    @Test
    @Order(16)
    @WithMockUser(authorities = "user")
    void getFriendRequestsFromAccountUsernameGivenNoFriendRequestsReturnsEmptyList() throws Exception {

        mockMvc.perform(get("/api/friends/requests/LuckyPlayer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Order(17)
    @WithMockUser(authorities = "user")
    void inviteFriendToGameGivenValidUsernameReturnsOkStatus() throws Exception {

        mockMvc.perform(post("/api/friends/invite/afi/3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"robbe\"]"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(18)
    @WithMockUser(authorities = "user")
    void inviteFriendToGameGivenInvalidUsernameReturnsNotFoundStatus() {

        ServletException exception = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/friends/invite/afi/3e8c27df-6e15-426b-9c76-5825d61183f7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"afiiiiii\"]")));

        assertEquals(AccountNotFoundException.class, exception.getCause().getClass());
    }
}