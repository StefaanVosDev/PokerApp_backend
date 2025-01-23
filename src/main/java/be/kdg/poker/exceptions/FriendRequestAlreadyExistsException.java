package be.kdg.poker.exceptions;

import lombok.Getter;

@Getter
public class FriendRequestAlreadyExistsException extends RuntimeException {
    public enum Type {
        ACCOUNT_TO_FRIEND,
        FRIEND_TO_ACCOUNT
    }

    private final Type type;

    public FriendRequestAlreadyExistsException(String message, Type type) {
        super(message);
        this.type = type;
    }

}
