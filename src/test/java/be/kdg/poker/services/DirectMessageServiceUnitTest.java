package be.kdg.poker.services;


import be.kdg.poker.TestcontainersConfiguration;
import be.kdg.poker.controllers.dto.DirectMessageDto;
import be.kdg.poker.domain.Account;
import be.kdg.poker.domain.DirectMessage;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.repositories.AccountRepository;
import be.kdg.poker.repositories.DirectMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DirectMessageServiceUnitTest {

    @MockBean
    private DirectMessageRepository directMessageRepository;
    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private DirectMessageService directMessageService;


    @Test
    void getDirectMessages_ShouldReturnDirectMessages_GivenValidSenderAndReceiver() {
        // Arrange
        String senderUsername = "afi";
        String receiverUsername = "robbe";

        Account sender = new Account();
        sender.setUsername(senderUsername);

        Account receiver = new Account();
        receiver.setUsername(receiverUsername);

        DirectMessage message = new DirectMessage(sender, receiver, "Hello Robbe!");
        List<DirectMessage> directMessages = List.of(message);

        when(accountRepository.findAccountByUsername(senderUsername)).thenReturn(Optional.of(sender));
        when(accountRepository.findAccountByUsername(receiverUsername)).thenReturn(Optional.of(receiver));
        when(directMessageRepository.findDirectMessagesBySenderAndReceiver(sender, receiver))
                .thenReturn(Optional.of(directMessages));

        // Act
        List<DirectMessageDto> result = directMessageService.getDirectMessages(receiverUsername, senderUsername);

        // Assert
        assertEquals(1, result.size());
        assertEquals("afi", result.get(0).sender());
        assertEquals("robbe", result.get(0).receiver());
        assertEquals("Hello Robbe!", result.get(0).message());
    }

    @Test
    void getDirectMessages_ShouldThrowAccountNotFoundException_WhenSenderDoesNotExist() {
        // Arrange
        String senderUsername = "unknown";
        String receiverUsername = "robbe";

        when(accountRepository.findAccountByUsername(senderUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                directMessageService.getDirectMessages(receiverUsername, senderUsername)
        );
    }

    @Test
    void getDirectMessages_ShouldReturnEmptyList_WhenNoMessagesExist() {
        // Arrange
        String senderUsername = "afi";
        String receiverUsername = "robbe";

        Account sender = new Account();
        sender.setUsername(senderUsername);

        Account receiver = new Account();
        receiver.setUsername(receiverUsername);

        when(accountRepository.findAccountByUsername(senderUsername)).thenReturn(Optional.of(sender));
        when(accountRepository.findAccountByUsername(receiverUsername)).thenReturn(Optional.of(receiver));
        when(directMessageRepository.findDirectMessagesBySenderAndReceiver(sender, receiver))
                .thenReturn(Optional.empty());

        // Act
        List<DirectMessageDto> result = directMessageService.getDirectMessages(receiverUsername, senderUsername);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void sendDirectMessage_ShouldSaveMessage_GivenValidSenderAndReceiver() {
        // Arrange
        String senderUsername = "afi";
        String receiverUsername = "robbe";
        String messageContent = "Hello Robbe!";

        Account sender = new Account();
        sender.setUsername(senderUsername);

        Account receiver = new Account();
        receiver.setUsername(receiverUsername);

        DirectMessageDto directMessageDto = new DirectMessageDto(senderUsername, receiverUsername, messageContent, null, null, false);

        when(accountRepository.findAccountByUsername(senderUsername)).thenReturn(Optional.of(sender));
        when(accountRepository.findAccountByUsername(receiverUsername)).thenReturn(Optional.of(receiver));

        // Act
        directMessageService.sendDirectMessage(directMessageDto);

        // Assert
        verify(directMessageRepository).save(argThat(directMessage ->
                directMessage.getSender().equals(sender) &&
                        directMessage.getReceiver().equals(receiver) &&
                        directMessage.getContent().equals(messageContent)
        ));
    }

    @Test
    void sendDirectMessage_ShouldThrowAccountNotFoundException_WhenReceiverDoesNotExist() {
        // Arrange
        String senderUsername = "afi";
        String receiverUsername = "unknown";
        String messageContent = "Hello Robbe!";

        DirectMessageDto directMessageDto = new DirectMessageDto(senderUsername, receiverUsername, messageContent, null, null, false);

        Account sender = new Account();
        sender.setUsername(senderUsername);

        when(accountRepository.findAccountByUsername(senderUsername)).thenReturn(Optional.of(sender));
        when(accountRepository.findAccountByUsername(receiverUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                directMessageService.sendDirectMessage(directMessageDto)
        );
    }




}
