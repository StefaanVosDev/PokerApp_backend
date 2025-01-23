package be.kdg.poker.domain.events.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserActivityEvent {
    private String userId;
    private String activity; // Bijv. "login", "logout", "play_game", etc.
    private LocalDateTime timestamp;

    public UserActivityEvent(String userId, String activity, LocalDateTime timestamp) {
        this.userId = userId;
        this.activity = activity;
        this.timestamp = timestamp;
    }

    public UserActivityEvent() {
    }

    @Override
    public String toString() {
        return "UserActivityEvent{" +
                "userId='" + userId + '\'' +
                ", activity='" + activity + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
