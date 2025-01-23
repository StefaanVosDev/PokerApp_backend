package be.kdg.poker.domain.events.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameBetEvent {
    private String userId;
    private String gameId;
    private float betAmount;
    private LocalDateTime betTimestamp;

    // Constructor
    public GameBetEvent(String userId, String gameId, float betAmount, LocalDateTime betTimestamp) {
        this.userId = userId;
        this.gameId = gameId;
        this.betAmount = betAmount;
        this.betTimestamp = betTimestamp;
    }

    // Default constructor (required for serialization/deserialization)
    public GameBetEvent() {
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "GameBetEvent{" +
                "userId='" + userId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", betAmount=" + betAmount +
                ", betTimestamp=" + betTimestamp +
                '}';
    }
}
