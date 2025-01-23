package be.kdg.poker.controllers;


import be.kdg.poker.controllers.dto.DirectMessageDto;
import be.kdg.poker.exceptions.AccountNotFoundException;
import be.kdg.poker.services.DirectMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/directMessages")
public class DirectMessagesController {

    private final DirectMessageService directMessageService;


    public DirectMessagesController(DirectMessageService directMessageService) {
        this.directMessageService = directMessageService;
    }

    @GetMapping("/{senderUsername}/{receiverUsername}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<List<DirectMessageDto>> getDirectMessages(@PathVariable String receiverUsername, @PathVariable String senderUsername) {
        log.info("Received request to get direct messages for receiver: {} and sender: {}", receiverUsername, senderUsername);
        List<DirectMessageDto> directMessages = directMessageService.getDirectMessages(receiverUsername, senderUsername);
        if (directMessages.isEmpty()) {
            log.info("No direct messages found for receiver: {} and sender: {}", receiverUsername, senderUsername);
            return ResponseEntity.noContent().build();
        }
        log.info("Successfully retrieved direct messages for receiver: {} and sender: {}", receiverUsername, senderUsername);
        return ResponseEntity.ok(directMessages);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> sendDirectMessage(@RequestBody DirectMessageDto directMessageDto) {
        log.info("Received request to send direct message from sender: {} to receiver: {}", directMessageDto.sender(), directMessageDto.receiver());
        directMessageService.sendDirectMessage(directMessageDto);
        log.info("Successfully sent direct message from sender: {} to receiver: {}", directMessageDto.sender(), directMessageDto.receiver());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{receiver}/{sender}")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Void> markAsRead(@PathVariable String receiver, @PathVariable String sender) {
        log.info("received request to mark messages between {} and {} as read", receiver, sender);
        try {
            directMessageService.markAllAsRead(receiver, sender);
            return ResponseEntity.ok().build();
        } catch (AccountNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
