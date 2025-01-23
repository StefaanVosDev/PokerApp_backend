package be.kdg.poker.domain.events.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameDecisionEvent {
    private String userId;
    private String gameId;
    private String decision; // Examples: "fold", "call", "raise"
    private int decisionSpeed; // Time taken to make the decision, in milliseconds
    private LocalDateTime timestamp;

    // Constructors
    public GameDecisionEvent() {}

    public GameDecisionEvent(String userId, String gameId, String decision, int decisionSpeed, LocalDateTime timestamp) {
        this.userId = userId;
        this.gameId = gameId;
        this.decision = decision;
        this.decisionSpeed = decisionSpeed;
        this.timestamp = timestamp;
    }

    // toString Method
    @Override
    public String toString() {
        return "GameDecisionEvent{" +
                "userId='" + userId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", decision='" + decision + '\'' +
                ", decisionSpeed=" + decisionSpeed +
                ", timestamp=" + timestamp +
                '}';
    }
}
