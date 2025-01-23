package be.kdg.poker.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class DirectMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Account sender;

    @ManyToOne
    private Account receiver;

    @Size(min = 1, max = 255)
    private String content;

    private UUID gameId;

    private LocalDateTime timestamp;
    private boolean read;

    public DirectMessage() {
    }

    public DirectMessage(Account sender, Account receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public DirectMessage(Account sender, Account receiver, String content, UUID gameId) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.gameId = gameId;
    }


}
