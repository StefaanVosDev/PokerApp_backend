package be.kdg.poker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class GameNotification extends Notification {
    @ManyToOne
    private Game game;

    public GameNotification() {
    }

    public GameNotification(Game game, String message, Account account) {
        super(message, account);
        this.game = game;
    }
}
