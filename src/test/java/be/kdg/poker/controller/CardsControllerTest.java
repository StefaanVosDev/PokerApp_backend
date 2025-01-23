package be.kdg.poker.controller;

import be.kdg.poker.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "user")
    void getPlayersHandGivenValidPlayerIdShouldReturnPlayersHand() throws Exception {
        mockMvc.perform(get("/api/cards/player/fbe9bdbf-4329-4fea-9bed-0449d5677804")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("fbe9bdbf-4329-4fea-9bed-0449d5677804"))
                .andExpect(jsonPath("$.hand").isArray())
                .andExpect(jsonPath("$.hand.size()").value(greaterThan(0)))
                .andDo(print());
    }

    @Test
    @WithMockUser(authorities = "user")
    void getPlayersHandGivenInvalidPlayerIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/cards/player/fbe9bdbf-4329-4fea-9bed-0449d5677801")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
