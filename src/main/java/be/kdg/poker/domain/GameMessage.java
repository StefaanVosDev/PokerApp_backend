package be.kdg.poker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class GameMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player sender;

    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();
    @ManyToOne
    private Game game;

    public GameMessage() {
    }

    public GameMessage(Player sender, String content) {
        this.sender = sender;
        this.content = content;
    }
}
