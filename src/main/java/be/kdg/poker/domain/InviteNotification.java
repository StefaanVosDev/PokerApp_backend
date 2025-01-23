package be.kdg.poker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class InviteNotification extends Notification {
    @ManyToOne
    private Game game;

    private String sender;

    public InviteNotification() {
    }

    public InviteNotification(Game game, String message, Account account, String sender) {
        super(message, account);
        this.game = game;
        this.sender = sender;
    }
}
