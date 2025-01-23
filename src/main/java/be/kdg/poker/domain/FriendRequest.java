package be.kdg.poker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FriendRequest extends Notification {
    @ManyToOne
    private Account requestingFriend;

    public FriendRequest() {
    }

    public FriendRequest(String message, Account requestingFriend, Account account) {
        super(message, account);
        this.requestingFriend = requestingFriend;
    }
}
