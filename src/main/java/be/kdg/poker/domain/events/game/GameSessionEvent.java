package be.kdg.poker.domain.events.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameSessionEvent {
    private String userId;          // ID van de speler
    private String sessionId;       // ID van de sessie
    private String eventType;       // Type van het event: "game_started" of "game_ended"
    private LocalDateTime timestamp; // Tijdstip van het event

    public GameSessionEvent() {
    }

    public GameSessionEvent(String userId, String sessionId, String eventType, LocalDateTime timestamp) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GameSessionEvent{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
