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
public class DirectMessagesControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser(authorities = "user")
    void getDirectMessages_GivenValidReceiverAndSenderUsername_ShouldReturnDirectMessagesAndOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/directMessages/PlayerOne/ProPlayer").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sender").value("PlayerOne"))
                .andExpect(jsonPath("$[0].receiver").value("ProPlayer"))
                .andExpect(jsonPath("$[0].message").value("Hello Robbe!"))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(authorities = "user")
    void GetDirectMessages_GivenValidReceiverAndSenderUsername_ShouldReturnAnEmptyListOfDirectMessagesAndNoContentStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/directMessages/afi/PlayerOne"))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser(authorities = "user")
    void sendDirectMessage_GivenValidDirectMessageDto_ShouldReturnOkStatus() throws Exception {
        // Act and Assert
        mockMvc.perform(post("/api/directMessages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "sender": "afi",
                                    "receiver": "robbe",
                                    "message": "Hello, this is a test message"
                                }
                                """))
                .andExpect(status().isOk());
    }


}
