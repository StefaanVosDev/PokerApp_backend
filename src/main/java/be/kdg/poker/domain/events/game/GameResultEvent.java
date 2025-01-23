package be.kdg.poker.domain.events.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GameResultEvent {
    private String userId;          // ID van de speler
    private String gameId;          // ID van het spel
    private boolean win;            // Of de speler heeft gewonnen
    private int winAmount;        // Winstbedrag in dit spel
    private LocalDateTime timestamp; // Tijdstip van het game-resultaat

    public GameResultEvent() {
    }

    public GameResultEvent(String userId, String gameId, boolean win, int winAmount, LocalDateTime timestamp) {
        this.userId = userId;
        this.gameId = gameId;
        this.win = win;
        this.winAmount = winAmount;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GameResultEvent{" +
                "userId='" + userId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", win=" + win +
                ", winAmount=" + winAmount +
                ", timestamp=" + timestamp +
                '}';
    }
}
